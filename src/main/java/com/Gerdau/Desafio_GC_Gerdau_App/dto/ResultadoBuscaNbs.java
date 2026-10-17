package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.util.List;

/** EXATO: nome com mapeamento aprovado no De-Para. SUGESTOES: parecidos no catálogo NBS, ainda sem revisão. */
public record ResultadoBuscaNbs(Tipo tipo, List<SugestaoNbs> resultados) {

    public enum Tipo { EXATO, SUGESTOES }
}
