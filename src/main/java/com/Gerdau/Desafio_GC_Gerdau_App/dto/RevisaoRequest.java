package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import jakarta.validation.constraints.NotBlank;

/** motivo é obrigatório para rejeitar e revogar; o serviço valida. */
public record RevisaoRequest(@NotBlank String revisor, String motivo) {
}
