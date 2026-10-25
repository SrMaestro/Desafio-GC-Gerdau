package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;

import java.util.Set;

/**
 * Quais linhas enviadas de um lote a ação em bloco do aprovador alcança. {@code situacoes} nulo ou vazio = todas;
 * {@code scoreMinimo} nulo = sem corte por similaridade (com corte, linhas sem score ficam de fora).
 * Em qualquer caso só linhas na etapa {@code ENVIADA} são tocadas.
 */
public record FiltroAprovacao(Set<SituacaoLinha> situacoes, Double scoreMinimo) {
}
