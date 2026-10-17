package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.exception.DeParaNaoEncontradoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDeParaHistorico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaHistoricoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Fluxo de revisão do dicionário De-Para: o usuário só confirma (PENDENTE) e um revisor decide.
 * Apenas mapeamentos APROVADO valem como busca exata.
 */
@Service
public class DeParaService {

    private final DicionarioDeParaRepository repository;
    private final DicionarioDeParaHistoricoRepository historicoRepository;
    private final CatNbsRepository catNbsRepository;

    public DeParaService(DicionarioDeParaRepository repository,
                         DicionarioDeParaHistoricoRepository historicoRepository,
                         CatNbsRepository catNbsRepository) {
        this.repository = repository;
        this.historicoRepository = historicoRepository;
        this.catNbsRepository = catNbsRepository;
    }

    /** Minúsculo, sem acento e com espaços colapsados: chave de busca do dicionário. */
    public static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    /**
     * Registra a confirmação de um usuário como PENDENTE (ou soma uma confirmação se o par já está pendente).
     * Nome que já tem mapeamento aprovado é ignorado, porque a confirmação nunca altera um aprovado.
     *
     * @return true se a confirmação foi registrada
     */
    @Transactional
    public boolean confirmar(String nomeDigitado, String codigoNbs, Float score) {
        if (nomeDigitado == null || nomeDigitado.isBlank()) {
            throw new IllegalArgumentException("Informe o nome digitado");
        }
        if (codigoNbs == null || !catNbsRepository.existsById(codigoNbs)) {
            throw new IllegalArgumentException("Código NBS desconhecido: " + codigoNbs);
        }
        String nome = normalizar(nomeDigitado);
        if (repository.findByNomeNormalizadoAndStatus(nome, StatusDePara.APROVADO).isPresent()) {
            return false;
        }
        return repository.registrarConfirmacao(nome, nomeDigitado.trim(), codigoNbs, score) > 0;
    }

    public List<DicionarioDePara> listarFila() {
        return repository.findByStatusOrderByConfirmacoesDescCriadoEmAsc(StatusDePara.PENDENTE);
    }

    public List<DicionarioDePara> listarAprovados() {
        return repository.findByStatusOrderByConfirmacoesDescCriadoEmAsc(StatusDePara.APROVADO);
    }

    /** Aprova um pendente e rejeita os outros pendentes do mesmo nome. */
    @Transactional
    public DicionarioDePara aprovar(Long id, String revisor) {
        exigirTexto(revisor, "revisor");
        DicionarioDePara alvo = carregar(id);
        exigirStatus(alvo, StatusDePara.PENDENTE, "aprovado");

        boolean jaTemAprovado = repository
                .findByNomeNormalizadoAndStatus(alvo.getNomeNormalizado(), StatusDePara.APROVADO)
                .isPresent();
        if (jaTemAprovado) {
            throw new IllegalStateException(
                    "Já existe mapeamento aprovado para \"" + alvo.getNomeNormalizado() + "\"; revogue-o antes");
        }

        mudarStatus(alvo, StatusDePara.APROVADO, revisor, null);
        try {
            repository.saveAndFlush(alvo);
        } catch (DataIntegrityViolationException e) {
            // corrida: outro revisor aprovou o mesmo nome entre a checagem e o update
            throw new IllegalStateException(
                    "Outro revisor aprovou \"" + alvo.getNomeNormalizado() + "\" ao mesmo tempo", e);
        }

        repository.findByNomeNormalizadoAndStatusOrderByConfirmacoesDesc(alvo.getNomeNormalizado(), StatusDePara.PENDENTE)
                .stream()
                .filter(outro -> !outro.getId().equals(alvo.getId()))
                .forEach(outro -> {
                    mudarStatus(outro, StatusDePara.REJEITADO, revisor,
                            "Outro mapeamento foi aprovado para o mesmo nome");
                    repository.save(outro);
                });
        return alvo;
    }

    @Transactional
    public DicionarioDePara rejeitar(Long id, String revisor, String motivo) {
        exigirTexto(revisor, "revisor");
        exigirTexto(motivo, "motivo");
        DicionarioDePara alvo = carregar(id);
        exigirStatus(alvo, StatusDePara.PENDENTE, "rejeitado");
        mudarStatus(alvo, StatusDePara.REJEITADO, revisor, motivo);
        return repository.save(alvo);
    }

    /** Tira um mapeamento aprovado de circulação, mantendo o histórico. */
    @Transactional
    public DicionarioDePara revogar(Long id, String revisor, String motivo) {
        exigirTexto(revisor, "revisor");
        exigirTexto(motivo, "motivo");
        DicionarioDePara alvo = carregar(id);
        exigirStatus(alvo, StatusDePara.APROVADO, "revogado");
        mudarStatus(alvo, StatusDePara.REVOGADO, revisor, motivo);
        return repository.save(alvo);
    }

    private DicionarioDePara carregar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new DeParaNaoEncontradoException(id));
    }

    private void exigirStatus(DicionarioDePara mapeamento, StatusDePara esperado, String acao) {
        if (mapeamento.getStatus() != esperado) {
            throw new IllegalStateException("Só um mapeamento " + esperado + " pode ser " + acao
                    + " (atual: " + mapeamento.getStatus() + ")");
        }
    }

    private void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Informe o " + campo);
        }
    }

    private void mudarStatus(DicionarioDePara mapeamento, StatusDePara novo, String revisor, String motivo) {
        DicionarioDeParaHistorico historico = new DicionarioDeParaHistorico();
        historico.setDeParaId(mapeamento.getId());
        historico.setDeStatus(mapeamento.getStatus());
        historico.setParaStatus(novo);
        historico.setUsuario(revisor.trim());
        historico.setMotivo(motivo);
        historicoRepository.save(historico);

        mapeamento.setStatus(novo);
        mapeamento.setRevisadoPor(revisor.trim());
        mapeamento.setRevisadoEm(LocalDateTime.now());
        mapeamento.setMotivo(motivo);
    }
}
