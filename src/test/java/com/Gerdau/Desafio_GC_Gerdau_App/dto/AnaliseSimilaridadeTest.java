package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnaliseSimilaridadeTest {

    private static final List<SimilarDto> SIMILARES = List.of(new SimilarDto(1, "Texto", "COD", 0.85));

    @Test
    void bloqueioPorTextoEhContestavel() {
        assertThat(new AnaliseSimilaridade("COD", false, SIMILARES, true, null).contestavel()).isTrue();
    }

    @Test
    void bloqueioPorCodigoExistenteNaoEhContestavel() {
        assertThat(new AnaliseSimilaridade("COD", true, SIMILARES, true, null).contestavel()).isFalse();
    }

    @Test
    void bloqueioPorSinonimoAprovadoNaoEhContestavel() {
        assertThat(new AnaliseSimilaridade("COD", false, SIMILARES, true, "COD").contestavel()).isFalse();
    }

    @Test
    void semBloqueioNaoHaOQueContestar() {
        assertThat(new AnaliseSimilaridade("COD", false, SIMILARES, false, null).contestavel()).isFalse();
    }
}
