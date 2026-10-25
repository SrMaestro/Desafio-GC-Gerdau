package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/** O revisor é o usuário logado; motivo é obrigatório para rejeitar e revogar, e o serviço valida. */
public record RevisaoRequest(String motivo) {
}
