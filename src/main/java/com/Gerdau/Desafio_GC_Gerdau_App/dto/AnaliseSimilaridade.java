package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.util.List;

public record AnaliseSimilaridade(String codigoGerado, boolean codigoExistente, List<SimilarDto> similares, boolean bloqueado,
                                 String reconhecidoComo) {

    public record SimilarDto(Integer id, String textoBreve, String codigoPadronizado, double score) {
    }

    /** Só o bloqueio por texto parecido pode ser contestado: código idêntico e sinônimo aprovado não. */
    public boolean contestavel() {
        return bloqueado && !codigoExistente && reconhecidoComo == null;
    }

    public boolean requerConfirmacao() {
        return !bloqueado && !similares.isEmpty();
    }
}
