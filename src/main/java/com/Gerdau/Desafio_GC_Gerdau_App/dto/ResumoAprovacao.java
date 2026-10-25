package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/** Resultado de uma aprovação em bloco: linhas aprovadas e linhas que foram para conflito. */
public record ResumoAprovacao(int aprovadas, int conflitos) {
}
