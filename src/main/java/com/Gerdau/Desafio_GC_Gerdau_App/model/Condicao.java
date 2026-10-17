package com.Gerdau.Desafio_GC_Gerdau_App.model;

import java.util.Arrays;
import java.util.Optional;

/** Valores aceitos em id1_condicao (espelha o CHECK da V1) com o sufixo usado no código padronizado. */
public enum Condicao {
    HORA_NORMAL("Hora normal", "HN"),
    HORA_EXTRA_SEG_SAB("Hora extra Seg a Sab", "HES"),
    HORA_EXTRA_DOM_FERIADO("Hora extra Dom/Feriado", "HED"),
    ADICIONAL_NOTURNO("Adicional noturno (hora)", "NOT"),
    HORA_EXTRA_NOTURNA_SEG_SAB("Hora extra noturna Seg a Sab", "HENS"),
    HORA_EXTRA_NOTURNA_DOM_FERIADO("Hora extra noturna Dom/Feriado", "HEND"),
    ADICIONAL_PERICULOSIDADE_HORA("Adicional periculosidade (hora)", "PERIC"),
    ADICIONAL_PERICULOSIDADE_SEM_HE("Adicional periculosidade s/ hora extra", "PERS"),
    ADICIONAL_PERICULOSIDADE_MENSAL("Adicional periculosidade (mensal)", "PERM"),
    ADICIONAL_INSALUBRIDADE_HORA("Adicional insalubridade (hora)", "INSAL"),
    POSTO_MENSAL("Posto mensal (equipe fixa)", "POSTO"),
    MOBILIZACAO("Mobilizacao / Desmobilizacao", "MOB"),
    RETROATIVO("Retroativo", "RETRO"),
    OBSERVACAO("Obs.: Itens que não são mão de obra", "OBS");

    private final String descricao;
    private final String sufixo;

    Condicao(String descricao, String sufixo) {
        this.descricao = descricao;
        this.sufixo = sufixo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSufixo() {
        return sufixo;
    }

    public static Optional<Condicao> daSufixo(String sufixo) {
        if (sufixo == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(c -> c.sufixo.equalsIgnoreCase(sufixo.trim())).findFirst();
    }

    public static Condicao daDescricao(String descricao) {
        return Arrays.stream(values())
                .filter(c -> c.descricao.equals(descricao))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Condição desconhecida: " + descricao));
    }
}
