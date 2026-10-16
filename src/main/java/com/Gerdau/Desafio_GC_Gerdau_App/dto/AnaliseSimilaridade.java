package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.util.List;

public record AnaliseSimilaridade(String codigoGerado, boolean codigoExistente, List<SimilarDto> similares, boolean bloqueado) {

    public record SimilarDto(Integer id, String textoBreve, String codigoPadronizado, double score) {
    }

    public boolean requerConfirmacao() {
        return !bloqueado && !similares.isEmpty();
    }
}
