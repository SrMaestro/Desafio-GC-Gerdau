package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;

public record HorarioResponse(String sufixo, String descricao) {

    public static HorarioResponse de(CatHorario h) {
        return new HorarioResponse(h.getSufixoCodigo(), h.getDescricaoHorario());
    }
}
