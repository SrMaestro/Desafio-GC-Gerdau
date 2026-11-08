package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.config.MonitoramentoProperties;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.DuplicataPossivel;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.PainelMonitoramento;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Monitoramento contínuo da base: duplicatas possíveis (mesma regra de bloqueio do cadastro, ver
 * {@link ServicoService#LIMIAR_BLOQUEIO}), serviços sem NBS, fila de aprovação e tempo de aprovação.
 * A rotina semanal grava um retrato em {@code monitoramento_execucao} e registra alertas no log.
 */
@Service
public class MonitoramentoService {

    private static final Logger log = LoggerFactory.getLogger(MonitoramentoService.class);
    static final int MAX_PARES = 200;

    private final JdbcTemplate jdbc;
    private final LinhaImportacaoRepository linhaRepository;
    private final DicionarioDeParaRepository deParaRepository;
    private final MonitoramentoProperties limites;

    public MonitoramentoService(JdbcTemplate jdbc, LinhaImportacaoRepository linhaRepository,
                                DicionarioDeParaRepository deParaRepository, MonitoramentoProperties limites) {
        this.jdbc = jdbc;
        this.linhaRepository = linhaRepository;
        this.deParaRepository = deParaRepository;
        this.limites = limites;
    }

    /** O operador % usa o índice GIN da V14; o filtro de similaridade fica acima do limiar do pg_trgm (0,3). */
    List<DuplicataPossivel> duplicatasPossiveis() {
        return jdbc.query("""
                SELECT a.id_servico, a.codigo_padronizado, a.texto_breve,
                       b.id_servico, b.codigo_padronizado, b.texto_breve,
                       similarity(a.texto_normalizado, b.texto_normalizado) AS score,
                       (a.duplicata_legada OR b.duplicata_legada) AS legado
                FROM servicos_mock a
                JOIN servicos_mock b ON a.id_servico < b.id_servico
                 AND a.id1_condicao = b.id1_condicao AND a.id2_cnae = b.id2_cnae
                 AND normaliza_texto(a.texto_breve) % normaliza_texto(b.texto_breve)
                WHERE similarity(a.texto_normalizado, b.texto_normalizado) >= ?
                ORDER BY score DESC, a.id_servico
                LIMIT ?
                """,
                (rs, i) -> new DuplicataPossivel(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getInt(4), rs.getString(5), rs.getString(6), rs.getDouble(7), rs.getBoolean(8)),
                ServicoService.LIMIAR_BLOQUEIO, MAX_PARES);
    }

    long servicosSemNbs() {
        return jdbc.queryForObject("SELECT count(*) FROM servicos_mock WHERE codigo_nbs IS NULL", Long.class);
    }

    /** Média, em horas, entre o envio e a decisão das linhas já decididas; {@code null} sem nenhuma. */
    Double horasMediaAprovacao() {
        return jdbc.queryForObject("""
                SELECT avg(extract(epoch FROM (decidido_em - enviado_em))) / 3600.0
                FROM linhas_importacao
                WHERE etapa IN ('APROVADA', 'REJEITADA') AND enviado_em IS NOT NULL AND decidido_em IS NOT NULL
                """, Double.class);
    }

    /** Linhas EXISTENTE ou NOVA (sem decisão humana) sobre todas as classificadas sem erro; {@code null} sem linhas. */
    Integer percentualAutomatico() {
        return jdbc.queryForObject("""
                SELECT CASE WHEN count(*) FILTER (WHERE situacao <> 'ERRO') = 0 THEN NULL
                       ELSE round(100.0 * count(*) FILTER (WHERE situacao IN ('EXISTENTE', 'NOVA'))
                                  / count(*) FILTER (WHERE situacao <> 'ERRO'))::int END
                FROM linhas_importacao
                """, Integer.class);
    }

    LocalDateTime ultimaExecucao() {
        Timestamp t = jdbc.queryForObject("SELECT max(executado_em) FROM monitoramento_execucao", Timestamp.class);
        return t == null ? null : t.toLocalDateTime();
    }

    public PainelMonitoramento painel() {
        List<DuplicataPossivel> duplicatas = duplicatasPossiveis();
        long fila = linhaRepository.countByEtapa(EtapaLinha.ENVIADA);
        long pendentes = deParaRepository.countByStatus(StatusDePara.PENDENTE);
        Double horas = horasMediaAprovacao();
        List<String> alertas = new ArrayList<>();
        if (duplicatas.size() > limites.limiteDuplicatas()) {
            alertas.add("Duplicatas possíveis acima do limite (" + duplicatas.size() + " > " + limites.limiteDuplicatas() + ")");
        }
        if (fila > limites.limiteFilaAprovacao()) {
            alertas.add("Fila de aprovação acima do limite (" + fila + " > " + limites.limiteFilaAprovacao() + ")");
        }
        if (horas != null && horas > limites.limiteHorasAprovacao()) {
            alertas.add("Tempo médio de aprovação acima do limite (" + Math.round(horas) + " h > "
                    + limites.limiteHorasAprovacao() + " h)");
        }
        return new PainelMonitoramento(duplicatas.size(), duplicatas, servicosSemNbs(), fila, pendentes,
                percentualAutomatico(), horas, ultimaExecucao(), alertas);
    }

    /** Toda segunda às 7h (configurável em {@code monitoramento.cron}); falha só vira log. */
    @Scheduled(cron = "${monitoramento.cron:0 0 7 * * MON}")
    public void rotinaSemanal() {
        try {
            PainelMonitoramento p = painel();
            jdbc.update("INSERT INTO monitoramento_execucao (duplicatas_possiveis, servicos_sem_nbs, fila_aprovacao,"
                            + " depara_pendentes) VALUES (?, ?, ?, ?)",
                    p.duplicatasPossiveis(), p.servicosSemNbs(), p.filaAprovacao(), p.deParaPendentes());
            log.info("Monitoramento semanal: {} duplicatas possíveis, {} sem NBS, {} na fila de aprovação",
                    p.duplicatasPossiveis(), p.servicosSemNbs(), p.filaAprovacao());
            p.alertas().forEach(a -> log.warn("ALERTA de monitoramento: {}", a));
        } catch (RuntimeException e) {
            log.error("Falha na rotina semanal de monitoramento", e);
        }
    }
}
