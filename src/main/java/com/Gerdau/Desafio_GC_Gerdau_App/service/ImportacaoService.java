package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.config.AsyncConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoLote;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Importação em massa: lê o arquivo, cria o lote e classifica as linhas em segundo plano.
 * <p>
 * Não há transação externa: cada {@code save}/{@code saveAll} dos repositories confirma por conta própria, então
 * o lote fica visível para a thread de fundo e para a tela de progresso, e as linhas já gravadas sobrevivem a uma
 * falha posterior. O processamento vai para um {@link Executor} (sem depender de proxy {@code @Async}, que seria
 * ignorado numa chamada da própria classe).
 */
@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    static final int TAMANHO_BLOCO = 200;
    private static final int MAX_MENSAGEM = 500;
    // tamanho da coluna do nome do arquivo (V10__importacao_em_massa.sql); as das linhas ficam em PreparadorLinha
    static final int MAX_ARQUIVO_NOME = 255;

    private final LeitorPlanilha leitorPlanilha;
    private final PreparadorLinha preparador;
    private final LoteImportacaoRepository loteRepository;
    private final LinhaImportacaoRepository linhaRepository;
    private final Executor executor;

    public ImportacaoService(LeitorPlanilha leitorPlanilha, PreparadorLinha preparador,
                             LoteImportacaoRepository loteRepository, LinhaImportacaoRepository linhaRepository,
                             @Qualifier(AsyncConfig.IMPORTACAO_EXECUTOR) Executor executor) {
        this.leitorPlanilha = leitorPlanilha;
        this.preparador = preparador;
        this.loteRepository = loteRepository;
        this.linhaRepository = linhaRepository;
        this.executor = executor;
    }

    /** Valida e lê o arquivo, grava o lote (PROCESSANDO) e entrega o trabalho ao executor. Devolve o id do lote. */
    public Long iniciar(String nomeArquivo, byte[] conteudo, String usuario) {
        List<LinhaPlanilha> linhas = leitorPlanilha.ler(nomeArquivo, conteudo); // ArquivoInvalidoException antes do lote
        LoteImportacao lote = new LoteImportacao();
        lote.setArquivoNome(nomeDoArquivoQueCabe(nomeArquivo));
        lote.setEnviadoPor(usuario);
        lote.setStatus(StatusLote.PROCESSANDO);
        lote.setTotalLinhas(linhas.size());
        Long loteId = loteRepository.save(lote).getId();
        try {
            executor.execute(() -> processar(loteId, linhas, usuario));
        } catch (RuntimeException e) { // fila cheia ou executor encerrado
            marcarFalha(loteId, "Não foi possível iniciar o processamento: fila de importações cheia ou indisponível");
        }
        return loteId;
    }

    public void processar(Long loteId, List<LinhaPlanilha> linhas) {
        LoteImportacao lote = loteRepository.findById(loteId).orElse(null);
        processar(loteId, linhas, lote == null ? null : lote.getEnviadoPor());
    }

    void processar(Long loteId, List<LinhaPlanilha> linhas, String usuario) {
        LoteImportacao lote = null;
        try {
            lote = loteRepository.findById(loteId).orElse(null);
            if (lote == null) {
                log.warn("Lote de importação {} não encontrado", loteId);
                return;
            }
            String criadorPadrao = usuario != null ? usuario : lote.getEnviadoPor();
            Set<String> codigosVistos = new HashSet<>();
            List<LinhaImportacao> bloco = new ArrayList<>(TAMANHO_BLOCO);
            int processadas = 0;
            for (LinhaPlanilha linha : linhas) {
                bloco.add(classificarELinha(lote, linha, criadorPadrao, codigosVistos));
                if (bloco.size() == TAMANHO_BLOCO) {
                    processadas += gravarBloco(lote, bloco, processadas);
                }
            }
            if (!bloco.isEmpty()) {
                processadas += gravarBloco(lote, bloco, processadas);
            }
            lote.setStatus(StatusLote.CONCLUIDO);
            loteRepository.save(lote);
        } catch (Exception e) {
            log.error("Falha ao processar o lote de importação {}", loteId, e);
            marcarFalha(lote, loteId, mensagemDe(e));
        }
    }

    private LinhaImportacao classificarELinha(LoteImportacao lote, LinhaPlanilha linha, String criadorPadrao,
                                              Set<String> codigosVistos) {
        LinhaImportacao l = new LinhaImportacao();
        l.setLote(lote);
        preparador.preencher(l, linha, criadorPadrao, codigosVistos);
        return l;
    }

    /** Se passar da coluna, corta o nome mas preserva a extensão. */
    private static String nomeDoArquivoQueCabe(String nome) {
        if (nome == null || nome.length() <= MAX_ARQUIVO_NOME) {
            return nome;
        }
        int ponto = nome.lastIndexOf('.');
        String extensao = ponto >= 0 && nome.length() - ponto <= 10 ? nome.substring(ponto) : "";
        return nome.substring(0, MAX_ARQUIVO_NOME - extensao.length()) + extensao;
    }

    /** Grava o bloco (confirma sozinho) e atualiza o progresso; devolve quantas linhas gravou. */
    private int gravarBloco(LoteImportacao lote, List<LinhaImportacao> bloco, int jaProcessadas) {
        int n = bloco.size();
        linhaRepository.saveAll(new ArrayList<>(bloco));
        bloco.clear();
        lote.setProcessadas(jaProcessadas + n);
        loteRepository.save(lote);
        return n;
    }

    private void marcarFalha(Long loteId, String mensagem) {
        marcarFalha(null, loteId, mensagem);
    }

    private void marcarFalha(LoteImportacao lote, Long loteId, String mensagem) {
        try {
            LoteImportacao alvo = lote != null ? lote : loteRepository.findById(loteId).orElse(null);
            if (alvo == null) {
                return;
            }
            alvo.setStatus(StatusLote.FALHOU);
            alvo.setMensagemErro(truncar(mensagem));
            loteRepository.save(alvo);
        } catch (RuntimeException e) {
            log.error("Não foi possível marcar o lote {} como FALHOU", loteId, e);
        }
    }

    private static String mensagemDe(Exception e) {
        if (e instanceof DataAccessException) {
            return "Falha ao acessar o banco de dados durante a importação. As linhas já gravadas foram mantidas.";
        }
        return "Erro inesperado durante a importação. As linhas já gravadas foram mantidas.";
    }

    private static String truncar(String s) {
        return s.length() <= MAX_MENSAGEM ? s : s.substring(0, MAX_MENSAGEM);
    }

    public ResumoLote resumo(Long loteId) {
        LoteImportacao lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote de importação não encontrado: " + loteId));
        Map<SituacaoLinha, Long> porSituacao = new EnumMap<>(SituacaoLinha.class);
        for (Object[] r : linhaRepository.contarPorSituacao(loteId)) {
            porSituacao.put((SituacaoLinha) r[0], ((Number) r[1]).longValue());
        }
        Map<EtapaLinha, Long> porEtapa = new EnumMap<>(EtapaLinha.class);
        for (Object[] r : linhaRepository.contarPorEtapa(loteId)) {
            porEtapa.put((EtapaLinha) r[0], ((Number) r[1]).longValue());
        }
        return new ResumoLote(lote.getId(), lote.getArquivoNome(), lote.getStatus(), lote.getTotalLinhas(),
                lote.getProcessadas(), porSituacao, porEtapa);
    }

    /** Lotes que ficaram PROCESSANDO quando a aplicação caiu não terminam mais: viram FALHOU. */
    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        try {
            for (LoteImportacao lote : loteRepository.findByStatus(StatusLote.PROCESSANDO)) {
                lote.setStatus(StatusLote.FALHOU);
                lote.setMensagemErro("A aplicação foi reiniciada durante a importação. Envie o arquivo novamente; "
                        + "as linhas já gravadas foram mantidas.");
                loteRepository.save(lote);
            }
        } catch (RuntimeException e) {
            log.error("Não foi possível encerrar lotes de importação pendentes", e);
        }
    }
}
