package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.SinonimoServicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * A unidade de trabalho da aprovação: uma linha, numa transação própria ({@code REQUIRES_NEW}). Fica num bean
 * separado de {@link AprovacaoImportacaoService} para que as chamadas passem pelo proxy do Spring (chamada interna
 * ignoraria o {@code @Transactional}), e assim um conflito numa linha nunca desfaz nem contamina as outras.
 */
@Component
class AprovadorLinha {

    /** A linha não pode ser aprovada; a transação é desfeita (nada gravado) e o motivo vai para a linha. */
    static class ConflitoAprovacao extends RuntimeException {
        /** Perdeu uma corrida no banco (unicidade): vale tentar de novo, que a checagem prévia dá o motivo exato. */
        private final boolean corrida;

        ConflitoAprovacao(String motivo) {
            this(motivo, false);
        }

        ConflitoAprovacao(String motivo, boolean corrida) {
            super(motivo, null, false, false);
            this.corrida = corrida;
        }

        boolean corrida() {
            return corrida;
        }
    }

    private final LinhaImportacaoRepository linhaRepository;
    private final ServicoRepository servicoRepository;
    private final SinonimoServicoService sinonimoServicoService;
    private final SinonimoServicoRepository sinonimoRepository;
    private final ClassificadorLinha classificador;

    AprovadorLinha(LinhaImportacaoRepository linhaRepository, ServicoRepository servicoRepository,
                   SinonimoServicoService sinonimoServicoService, SinonimoServicoRepository sinonimoRepository,
                   ClassificadorLinha classificador) {
        this.linhaRepository = linhaRepository;
        this.servicoRepository = servicoRepository;
        this.sinonimoServicoService = sinonimoServicoService;
        this.sinonimoRepository = sinonimoRepository;
        this.classificador = classificador;
    }

    /**
     * Aprova a linha com ela bloqueada ({@code SELECT ... FOR UPDATE}): outro aprovador na mesma linha espera e,
     * ao entrar, já a encontra fora de {@code ENVIADA}. Devolve {@code false} se a linha não está (mais) enviada.
     *
     * @throws ConflitoAprovacao se a linha não pode ser aprovada (nada é gravado)
     * @throws NoSuchElementException se a linha não existe
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean aprovar(Long linhaId, String aprovador) {
        LinhaImportacao linha = linhaRepository.findByIdParaAprovar(linhaId)
                .orElseThrow(() -> new NoSuchElementException("Linha de importação não encontrada: " + linhaId));
        if (linha.getEtapa() != EtapaLinha.ENVIADA) {
            return false;
        }
        Integer servicoCriadoId = null;
        if (linha.getSituacao() == SituacaoLinha.NOVA) {
            servicoCriadoId = criarServico(linha, true).getId();
        } else if (linha.getSituacao() == SituacaoLinha.PARECIDA) {
            if (linha.getDecisao() == DecisaoParecida.OUTRO_SERVICO) {
                // o comprador viu o parecido e decidiu que é outro serviço: sem a regra de bloqueio por texto
                servicoCriadoId = criarServico(linha, false).getId();
            } else if (linha.getDecisao() == DecisaoParecida.MESMO_SERVICO) {
                registrarSinonimo(linha, aprovador);
            } else {
                throw new ConflitoAprovacao("Linha parecida enviada sem a decisão do comprador");
            }
        } else {
            throw new ConflitoAprovacao("Linha na situação " + linha.getSituacao() + " não pode ser aprovada");
        }
        linha.setEtapa(EtapaLinha.APROVADA);
        linha.setDecididoPor(aprovador);
        linha.setDecididoEm(LocalDateTime.now());
        linha.setServicoCriadoId(servicoCriadoId);
        linhaRepository.save(linha);
        return true;
    }

    /** Põe a linha em CONFLITO com o motivo, se ainda estiver ENVIADA; devolve quantas linhas mudaram (0 ou 1). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int registrarConflito(Long linhaId, String aprovador, String motivo) {
        return linhaRepository.marcarConflitoSeEnviada(linhaId,
                PreparadorLinha.cortar(motivo, PreparadorLinha.MAX_MOTIVO), aprovador, LocalDateTime.now());
    }

    /** Rejeita a linha, se ainda estiver ENVIADA; devolve quantas linhas mudaram (0 ou 1). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int rejeitar(Long linhaId, String aprovador, String motivo) {
        return linhaRepository.rejeitarSeEnviada(linhaId,
                PreparadorLinha.cortar(motivo, PreparadorLinha.MAX_MOTIVO), aprovador, LocalDateTime.now());
    }

    /**
     * Cria o serviço da linha como o cadastro individual, depois de conferir de novo que o código é livre. Com
     * {@code bloquearPorTexto} (linha NOVA) aplica também a regra de bloqueio do cadastro individual contra a base
     * atual, que pega duplicados de texto com códigos diferentes, inclusive os aprovados antes no mesmo lote.
     */
    private Servico criarServico(LinhaImportacao linha, boolean bloquearPorTexto) {
        String codigo = linha.getCodigoGerado();
        if (codigo == null) {
            throw new ConflitoAprovacao("A linha não tem código gerado");
        }
        CatHorario condicao = condicao(linha);
        NovoServicoForm form = formulario(linha, cnae(linha));
        String codigoAtual = ServicoService.montarCodigo(form, condicao);
        if (!codigoAtual.equals(codigo)) {
            throw new ConflitoAprovacao("O código da linha (" + codigo + ") não confere com o catálogo atual ("
                    + codigoAtual + ")");
        }
        if (servicoRepository.findByCodigoPadronizado(codigo).isPresent()) {
            throw new ConflitoAprovacao("O código " + codigo + " já foi cadastrado por outra operação");
        }
        if (bloquearPorTexto && linha.getTextoBreve() != null) {
            Optional<ServicoSimilar> duplicado = ServicoService.duplicadoPorTexto(
                    ServicoService.buscarParecidos(servicoRepository, linha.getTextoBreve()),
                    condicao.getDescricaoHorario(), form.getCnae());
            if (duplicado.isPresent()) {
                throw new ConflitoAprovacao("Parecido com " + duplicado.get().getCodigoPadronizado()
                        + " (similaridade " + Math.round(duplicado.get().getScore() * 100)
                        + "%) já na base; verifique se é duplicado");
            }
        }
        try {
            return servicoRepository.saveAndFlush(ServicoService.novoServico(form, condicao, codigo));
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoAprovacao("O banco recusou o cadastro do código " + codigo
                    + " (provavelmente criado ao mesmo tempo por outra operação)", true);
        }
    }

    /** Liga o texto da linha ao serviço existente escolhido pelo comprador; já ligado ao mesmo serviço é sucesso. */
    private void registrarSinonimo(LinhaImportacao linha, String aprovador) {
        Integer servicoId = linha.getServicoParecidoId();
        if (servicoId == null) {
            throw new ConflitoAprovacao("A linha não indica o serviço existente");
        }
        Servico servico = servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ConflitoAprovacao("O serviço existente (id " + servicoId + ") não existe mais"));
        String texto = linha.getTextoBreve() == null ? null : linha.getTextoBreve().trim();
        if (texto == null || texto.isEmpty()) {
            throw new ConflitoAprovacao("A linha não tem texto breve");
        }
        String condicao = condicao(linha).getDescricaoHorario();
        String cnae = cnae(linha);
        // a chave do sinônimo usa a condição e o CNAE da linha: ligar a um serviço de outra condição/CNAE faria
        // o texto passar a ser reconhecido como um serviço que não é o dele
        if (!mesmaCondicao(servico.getId1Condicao(), condicao) || !mesmoCnae(servico.getId2Cnae(), cnae)) {
            throw new ConflitoAprovacao("O serviço parecido (" + servico.getCodigoPadronizado()
                    + ") é de outra condição/CNAE; use 'é outro serviço' ou corrija a linha");
        }
        Optional<Servico> ligado = sinonimoServicoService.reconhecer(texto, condicao, cnae);
        if (ligado.isPresent()) {
            if (Objects.equals(ligado.get().getId(), servico.getId())) {
                return; // aprovado antes (outra linha com o mesmo texto): nada a fazer
            }
            throw new ConflitoAprovacao("Este texto já está ligado a outro serviço: "
                    + ligado.get().getCodigoPadronizado());
        }
        try {
            sinonimoServicoService.registrar(texto, condicao, cnae, servico, aprovador, linha.getId());
            sinonimoRepository.flush(); // a violação de unicidade aparece aqui, não no commit
        } catch (DataIntegrityViolationException e) {
            throw new ConflitoAprovacao("O banco recusou o sinônimo (provavelmente registrado ao mesmo tempo "
                    + "por outra operação)", true);
        }
    }

    private static boolean mesmaCondicao(String doServico, String descricaoDaLinha) {
        return doServico != null && DeParaService.normalizar(doServico).equals(DeParaService.normalizar(descricaoDaLinha));
    }

    private static boolean mesmoCnae(String doServico, String cnaeDaLinha) {
        return doServico != null && doServico.replaceAll("\\D", "").equals(cnaeDaLinha);
    }

    private CatHorario condicao(LinhaImportacao linha) {
        return classificador.buscarCondicao(linha.getCondicao())
                .orElseThrow(() -> new ConflitoAprovacao("Condição desconhecida ou inativa: " + linha.getCondicao()));
    }

    private static String cnae(LinhaImportacao linha) {
        String cnae = ClassificadorLinha.normalizarCnae(linha.getCnae());
        if (cnae == null) {
            throw new ConflitoAprovacao("CNAE deve ter 7 dígitos: " + linha.getCnae());
        }
        return cnae;
    }

    private static NovoServicoForm formulario(LinhaImportacao linha, String cnae) {
        NovoServicoForm form = new NovoServicoForm();
        form.setAreaServico(linha.getAreaServico());
        form.setTextoBreve(linha.getTextoBreve());
        form.setAbreviatura(linha.getAbreviatura());
        form.setComplemento(linha.getComplemento());
        form.setCnae(cnae);
        form.setCodigoNbs(linha.getCodigoNbs());
        form.setCriadorCodigo(linha.getCriadorCodigo());
        return form;
    }
}
