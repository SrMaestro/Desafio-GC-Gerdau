package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;

/**
 * Resultado da classificação de uma linha da planilha. {@code codigoGerado} vem preenchido sempre que o código
 * pôde ser montado; {@code servicoParecidoId} e {@code score} só para EXISTENTE e PARECIDA.
 */
public record ResultadoClassificacao(
        SituacaoLinha situacao,
        String codigoGerado,
        Integer servicoParecidoId,
        Double score,
        String motivo) {
}
