package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.util.List;

/** Pendentes de um mesmo texto digitado; mais de um item significa sugestões em conflito. */
public record GrupoRevisao(String nome, List<DeParaResponse> itens) {

    public boolean isConflito() {
        return itens.size() > 1;
    }
}
