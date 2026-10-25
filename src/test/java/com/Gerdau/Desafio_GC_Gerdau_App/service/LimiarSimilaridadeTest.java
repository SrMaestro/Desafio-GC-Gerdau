package com.Gerdau.Desafio_GC_Gerdau_App.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ServicoRepository.buscarSimilares} usa o operador {@code %} para aproveitar o índice, e ele corta pelo
 * limiar do pg_trgm. Se esse limiar for >= ao mínimo de sugestão, parecidos somem em silêncio. Só leitura.
 */
@SpringBootTest
class LimiarSimilaridadeTest {

    @Autowired JdbcTemplate jdbc;

    @Test
    void limiarDoPgTrgmFicaAbaixoDoMinimoDeSugestao() {
        Float limiar = jdbc.queryForObject("SELECT show_limit()", Float.class);

        assertThat(limiar).isLessThan((float) ServicoService.LIMIAR_SUGESTAO);
    }
}
