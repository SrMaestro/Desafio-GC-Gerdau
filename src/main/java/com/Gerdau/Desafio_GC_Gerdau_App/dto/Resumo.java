package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/** Totais dos cards da lista de serviços; {@code null} significa contagem indisponível. */
public record Resumo(Long servicos, Long pendentes, Long aguardandoAprovacao) {
}
