package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/** Código final de um pedido: NBS + sufixo do horário, ex.: 1.0106.19.00-HN. */
public record SmartKey(String chave, String codigoNbs, String descricaoNbs, String sufixo, String descricaoHorario) {
}
