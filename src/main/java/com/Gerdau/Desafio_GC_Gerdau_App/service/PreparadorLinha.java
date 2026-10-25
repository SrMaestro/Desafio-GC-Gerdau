package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoClassificacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Transforma uma {@link LinhaPlanilha} nos campos de uma {@link LinhaImportacao}: classifica, confere repetição
 * com códigos já enviados em outros lotes, define a etapa inicial e faz os textos caberem nas colunas.
 * Usado na importação e na correção feita pelo comprador, para que as duas sigam exatamente a mesma regra.
 */
@Component
class PreparadorLinha {

    // tamanhos das colunas (V10__importacao_em_massa.sql e anotações de LinhaImportacao)
    static final int MAX_AREA_SERVICO = 50;
    static final int MAX_TEXTO_BREVE = 255;
    static final int MAX_ABREVIATURA = 10;
    static final int MAX_COMPLEMENTO = 15;
    static final int MAX_CONDICAO = 50;
    static final int MAX_CNAE = 15;
    static final int MAX_CODIGO_NBS = 20;
    static final int MAX_CRIADOR = 100;
    static final int MAX_CODIGO_GERADO = 40;
    static final int MAX_MOTIVO = 500;

    private final ClassificadorLinha classificador;
    private final LinhaImportacaoRepository linhaRepository;

    PreparadorLinha(ClassificadorLinha classificador, LinhaImportacaoRepository linhaRepository) {
        this.classificador = classificador;
        this.linhaRepository = linhaRepository;
    }

    /**
     * Preenche dados, classificação e etapa inicial de {@code l} (não mexe em lote, id nem decisões) e acrescenta
     * o código gerado a {@code codigosVistos}.
     */
    void preencher(LinhaImportacao l, LinhaPlanilha linha, String criadorPadrao, Set<String> codigosVistos) {
        ResultadoClassificacao r = classificador.classificar(linha, codigosVistos);
        SituacaoLinha situacao = r.situacao();
        String motivo = r.motivo();
        if ((situacao == SituacaoLinha.NOVA || situacao == SituacaoLinha.PARECIDA) && r.codigoGerado() != null
                && linhaRepository.existsByCodigoGeradoAndEtapaIn(r.codigoGerado(), Set.of(EtapaLinha.ENVIADA))) {
            situacao = SituacaoLinha.REPETIDA;
            motivo = "Este código já foi enviado em outro lote de importação";
        }
        if (r.codigoGerado() != null) {
            codigosVistos.add(r.codigoGerado());
        }
        l.setNumero(linha.numero());
        l.setAreaServico(cortar(linha.areaServico(), MAX_AREA_SERVICO));
        l.setTextoBreve(cortar(linha.textoBreve(), MAX_TEXTO_BREVE));
        l.setAbreviatura(cortar(linha.abreviatura(), MAX_ABREVIATURA));
        l.setComplemento(cortar(linha.complemento(), MAX_COMPLEMENTO));
        l.setCondicao(cortar(linha.condicao(), MAX_CONDICAO));
        l.setCnae(cortar(linha.cnae(), MAX_CNAE));
        l.setCodigoNbs(cortar(linha.codigoNbs(), MAX_CODIGO_NBS));
        boolean semCriador = linha.criadorCodigo() == null || linha.criadorCodigo().isBlank();
        l.setCriadorCodigo(cortar(semCriador ? criadorPadrao : linha.criadorCodigo(), MAX_CRIADOR));
        // código acima do limite não é gravado truncado (enganaria); o motivo mantém o texto completo
        String codigo = r.codigoGerado();
        l.setCodigoGerado(codigo != null && codigo.length() <= MAX_CODIGO_GERADO ? codigo : null);
        l.setSituacao(situacao);
        l.setEtapa(etapaInicial(situacao));
        l.setServicoParecidoId(r.servicoParecidoId());
        l.setScore(r.score());
        l.setMotivo(cortar(motivo, MAX_MOTIVO));
    }

    static EtapaLinha etapaInicial(SituacaoLinha situacao) {
        return switch (situacao) {
            case ERRO, PARECIDA -> EtapaLinha.EM_CONFERENCIA;
            case NOVA -> EtapaLinha.PRONTA;
            case EXISTENTE, REPETIDA -> EtapaLinha.IGNORADA;
        };
    }

    /** Corta o texto no tamanho da coluna (nulo continua nulo). */
    static String cortar(String valor, int max) {
        return valor == null || valor.length() <= max ? valor : valor.substring(0, max);
    }
}
