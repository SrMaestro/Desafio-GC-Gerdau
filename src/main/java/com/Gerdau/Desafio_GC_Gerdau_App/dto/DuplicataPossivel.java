package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/** Par de serviços do catálogo com texto muito parecido, mesma condição e mesmo CNAE. */
public record DuplicataPossivel(Integer idA, String codigoA, String textoA,
                                Integer idB, String codigoB, String textoB,
                                double similaridade, boolean legado) {
}
