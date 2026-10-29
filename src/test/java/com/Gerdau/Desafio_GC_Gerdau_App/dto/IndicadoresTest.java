package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndicadoresTest {

    @Test
    void percentualEvitado_somaBarradasEReutilizadasSobreOTotal() {
        assertThat(new Indicadores(10L, 3L, 2L, 4L).percentualEvitado()).isEqualTo(50);
    }

    @Test
    void percentualEvitado_arredondaParaOInteiroMaisProximo() {
        assertThat(new Indicadores(3L, 1L, 0L, 2L).percentualEvitado()).isEqualTo(33);
        assertThat(new Indicadores(3L, 1L, 1L, 1L).percentualEvitado()).isEqualTo(67);
    }

    @Test
    void percentualEvitado_semTentativasEhNull() {
        assertThat(new Indicadores(0L, 0L, 0L, 0L).percentualEvitado()).isNull();
    }

    @Test
    void percentualEvitado_comContagemIndisponivelEhNull() {
        assertThat(new Indicadores(null, 3L, 2L, 4L).percentualEvitado()).isNull();
        assertThat(new Indicadores(10L, null, 2L, 4L).percentualEvitado()).isNull();
        assertThat(new Indicadores(10L, 3L, null, 4L).percentualEvitado()).isNull();
    }

    @Test
    void percentualEvitado_todasEvitadasEh100() {
        assertThat(new Indicadores(4L, 3L, 1L, 0L).percentualEvitado()).isEqualTo(100);
    }
}
