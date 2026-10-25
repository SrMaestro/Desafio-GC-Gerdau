package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/**
 * Resultado de uma aprovação em bloco: linhas aprovadas, linhas que foram para conflito (de negócio) e linhas
 * que falharam por erro técnico. As {@code falhas} continuam {@code ENVIADA} e podem ser aprovadas de novo.
 */
public record ResumoAprovacao(int aprovadas, int conflitos, int falhas) {
}
