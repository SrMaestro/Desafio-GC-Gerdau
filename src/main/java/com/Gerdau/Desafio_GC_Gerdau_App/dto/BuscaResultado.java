package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.util.List;

/** origem: EXATA (achou no dicionário De-Para) ou FUZZY (similaridade no catálogo NBS). */
public record BuscaResultado(String origem, List<SugestaoDto> sugestoes) {
}
