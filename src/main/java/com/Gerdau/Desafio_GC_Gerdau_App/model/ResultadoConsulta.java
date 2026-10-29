package com.Gerdau.Desafio_GC_Gerdau_App.model;

/** O que o sistema respondeu à tentativa de cadastro. */
public enum ResultadoConsulta {
    BLOQUEADO("Bloqueado"),
    REQUER_CONFIRMACAO("Pediu confirmação"),
    CADASTRADO("Cadastrado"),
    SUGERIDO("Sugeriu parecidos");

    private final String rotulo;

    ResultadoConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
