package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.FiltroAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.AprovadorLinha.ConflitoAprovacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Decisão do aprovador sobre as linhas enviadas: aprovar (cria o serviço ou registra o sinônimo) ou rejeitar,
 * uma a uma ou em bloco. Só linhas na etapa {@code ENVIADA} são tocadas. O aprovador vê todos os lotes, então
 * não há conferência de dono aqui (o acesso por perfil fica nos endpoints).
 *
 * <p>De propósito <b>não</b> é {@code @Transactional}: cada linha roda numa transação própria em
 * {@link AprovadorLinha}. Linha que não pode ser aprovada vai para {@code CONFLITO} com o motivo, gravado noutra
 * transação depois que a da aprovação foi desfeita (no Postgres, uma violação de unicidade aborta a transação).
 */
@Service
public class AprovacaoImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(AprovacaoImportacaoService.class);
    /** Uma corrida perdida no banco é tentada de novo uma vez; a checagem prévia então dá o motivo exato. */
    private static final int MAX_TENTATIVAS = 2;

    private enum Desfecho { APROVADA, CONFLITO, FORA_DE_ENVIADA }

    private final AprovadorLinha aprovadorLinha;
    private final LinhaImportacaoRepository linhaRepository;

    AprovacaoImportacaoService(AprovadorLinha aprovadorLinha, LinhaImportacaoRepository linhaRepository) {
        this.aprovadorLinha = aprovadorLinha;
        this.linhaRepository = linhaRepository;
    }

    /**
     * Aprova uma linha enviada. Devolve {@code APROVADA} ou {@code CONFLITO} (o motivo fica na linha).
     *
     * @throws IllegalStateException se a linha não está aguardando aprovação
     * @throws NoSuchElementException se a linha não existe
     */
    public EtapaLinha aprovar(Long linhaId, String aprovador) {
        return switch (processar(linhaId, aprovador)) {
            case APROVADA -> EtapaLinha.APROVADA;
            case CONFLITO -> EtapaLinha.CONFLITO;
            case FORA_DE_ENVIADA -> throw naoEnviada(linhaId);
        };
    }

    /**
     * @throws IllegalArgumentException sem motivo
     * @throws IllegalStateException se a linha não está aguardando aprovação
     */
    public void rejeitar(Long linhaId, String aprovador, String motivo) {
        String m = motivoObrigatorio(motivo);
        if (aprovadorLinha.rejeitar(linhaId, aprovador, m) == 0) {
            throw naoEnviada(linhaId);
        }
    }

    /** Aprova as linhas enviadas do lote que atendem ao filtro, cada uma na sua transação. */
    public ResumoAprovacao aprovarEmBloco(Long loteId, FiltroAprovacao filtro, String aprovador) {
        int aprovadas = 0;
        int conflitos = 0;
        for (Long id : idsEnviadas(loteId, filtro)) {
            Desfecho d;
            try {
                d = processar(id, aprovador);
            } catch (NoSuchElementException e) {
                continue;
            }
            if (d == Desfecho.APROVADA) {
                aprovadas++;
            } else if (d == Desfecho.CONFLITO) {
                conflitos++;
            }
        }
        return new ResumoAprovacao(aprovadas, conflitos);
    }

    /** Rejeita as linhas enviadas do lote que atendem ao filtro; devolve quantas foram rejeitadas. */
    public int rejeitarEmBloco(Long loteId, FiltroAprovacao filtro, String aprovador, String motivo) {
        String m = motivoObrigatorio(motivo);
        int rejeitadas = 0;
        for (Long id : idsEnviadas(loteId, filtro)) {
            rejeitadas += aprovadorLinha.rejeitar(id, aprovador, m);
        }
        return rejeitadas;
    }

    private List<Long> idsEnviadas(Long loteId, FiltroAprovacao filtro) {
        Set<SituacaoLinha> situacoes = filtro == null || filtro.situacoes() == null || filtro.situacoes().isEmpty()
                ? EnumSet.allOf(SituacaoLinha.class) : filtro.situacoes();
        Double scoreMinimo = filtro == null ? null : filtro.scoreMinimo();
        return linhaRepository.idsEnviadasDoLote(loteId, situacoes, scoreMinimo);
    }

    private Desfecho processar(Long linhaId, String aprovador) {
        for (int tentativa = 1; ; tentativa++) {
            boolean ultima = tentativa >= MAX_TENTATIVAS;
            try {
                return aprovadorLinha.aprovar(linhaId, aprovador) ? Desfecho.APROVADA : Desfecho.FORA_DE_ENVIADA;
            } catch (ConflitoAprovacao e) {
                if (e.corrida() && !ultima) {
                    continue;
                }
                return conflito(linhaId, aprovador, e.getMessage());
            } catch (NoSuchElementException e) {
                throw e;
            } catch (DataIntegrityViolationException e) {
                // violação que só apareceu na confirmação da transação: nada foi gravado
                if (!ultima) {
                    continue;
                }
                return conflito(linhaId, aprovador, "O banco recusou a gravação desta linha");
            } catch (RuntimeException e) {
                log.warn("Falha inesperada ao aprovar a linha de importação {}", linhaId, e);
                return conflito(linhaId, aprovador, "Falha inesperada ao aprovar a linha; nada foi gravado");
            }
        }
    }

    /** Grava o conflito; se outra operação decidiu a linha antes, ela não conta como conflito. */
    private Desfecho conflito(Long linhaId, String aprovador, String motivo) {
        return aprovadorLinha.registrarConflito(linhaId, aprovador, motivo) == 1
                ? Desfecho.CONFLITO : Desfecho.FORA_DE_ENVIADA;
    }

    private static String motivoObrigatorio(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Informe o motivo da rejeição");
        }
        return motivo.trim();
    }

    private static IllegalStateException naoEnviada(Long linhaId) {
        return new IllegalStateException("A linha de importação " + linhaId
                + " não está aguardando aprovação (já foi decidida ou ainda não foi enviada)");
    }
}
