package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/**
 * Linha de dados de uma planilha de importação, já sem espaços nas pontas (vazio vira {@code null}).
 * {@code numero} é a posição na planilha: a primeira linha de dados é a 2, depois do cabeçalho.
 */
public record LinhaPlanilha(
        int numero,
        String areaServico,
        String textoBreve,
        String abreviatura,
        String complemento,
        String condicao,
        String cnae,
        String codigoNbs,
        String criadorCodigo) {
}
