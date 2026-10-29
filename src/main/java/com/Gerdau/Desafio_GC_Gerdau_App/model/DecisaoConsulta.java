package com.Gerdau.Desafio_GC_Gerdau_App.model;

/** O que o usuário decidiu depois de ver (ou não) as sugestões. */
public enum DecisaoConsulta {
    CADASTROU_NOVO("Cadastrou novo"),
    CONFIRMOU_MESMO_ASSIM("Cadastrou mesmo assim"),
    REUTILIZOU_EXISTENTE("Reutilizou código existente"),
    CONTESTOU_BLOQUEIO("Contestou o bloqueio");

    private final String rotulo;

    DecisaoConsulta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
