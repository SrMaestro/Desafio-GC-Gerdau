package com.Gerdau.Desafio_GC_Gerdau_App.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CondicaoTest {

    @Test
    void daSufixo_encontraIgnorandoCaixaEEspacos() {
        assertEquals(Condicao.HORA_EXTRA_SEG_SAB, Condicao.daSufixo(" hes ").orElseThrow());
        assertEquals(Condicao.HORA_NORMAL, Condicao.daSufixo("HN").orElseThrow());
    }

    @Test
    void daSufixo_devolveVazioParaDesconhecidoOuNulo() {
        assertTrue(Condicao.daSufixo("XX").isEmpty());
        assertTrue(Condicao.daSufixo(null).isEmpty());
    }
}
