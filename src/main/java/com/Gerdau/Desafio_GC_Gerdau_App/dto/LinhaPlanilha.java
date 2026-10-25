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

    /**
     * A mesma linha com todos os campos sem espaços nas pontas e os vazios como {@code null}. A tela e a API mandam
     * {@code ""} nos campos que o comprador deixou em branco; importação, correção e classificação usam esta forma
     * para que {@code ""} e {@code null} deem sempre o mesmo resultado.
     */
    public LinhaPlanilha limpa() {
        return new LinhaPlanilha(numero, limpar(areaServico), limpar(textoBreve), limpar(abreviatura),
                limpar(complemento), limpar(condicao), limpar(cnae), limpar(codigoNbs), limpar(criadorCodigo));
    }

    private static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }
}
