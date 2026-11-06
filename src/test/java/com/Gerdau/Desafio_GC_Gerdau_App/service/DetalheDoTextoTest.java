package com.Gerdau.Desafio_GC_Gerdau_App.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DetalheDoTextoTest {

    @Test
    void numerosDiferentesNaoSaoCompativeis() {
        assertThat(DetalheDoTexto.compativeis("Manutenção de bomba centrífuga 10 cv",
                "Manutenção de bomba centrífuga 20 cv")).isFalse();
        assertThat(DetalheDoTexto.compativeis("Solda TIG em aço inox 304", "Solda TIG em aço inox 316")).isFalse();
    }

    @Test
    void palavraFinalDiferenteNaoEhCompativel() {
        assertThat(DetalheDoTexto.compativeis("Pintura epóxi industrial interna",
                "Pintura epóxi industrial externa")).isFalse();
    }

    @Test
    void mesmoServicoComPequenasVariacoesEhCompativel() {
        assertThat(DetalheDoTexto.compativeis("Solda TIG aço inox", "Solda TIG em aço inox")).isTrue();
        assertThat(DetalheDoTexto.compativeis("Aço inox solda TIG", "Solda TIG em aço inox")).isTrue();
        assertThat(DetalheDoTexto.compativeis("Manutenção do motor elétrico",
                "Manutenção de motor elétrico")).isTrue();
        assertThat(DetalheDoTexto.compativeis("Instalação de painel eletrico",
                "Instalação de painel elétrico")).isTrue();
        assertThat(DetalheDoTexto.compativeis("Pintura de tubulação", "Pintura de tubulações")).isTrue();
        assertThat(DetalheDoTexto.compativeis("Calibração de instrumento de campo",
                "Calibração de instrumentos de campo")).isTrue();
    }

    @Test
    void textoNuloNaoImpedeOBloqueio() {
        assertThat(DetalheDoTexto.compativeis("qualquer", null)).isTrue();
    }
}
