package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmacaoRequest(
        @NotBlank String nomeDigitado,
        @NotBlank String codigoNbs,
        Float score) {
}
