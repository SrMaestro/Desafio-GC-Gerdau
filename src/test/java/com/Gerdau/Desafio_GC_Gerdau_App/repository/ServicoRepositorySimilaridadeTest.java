package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Prende o contrato de {@code buscarSimilares} (usa o Postgres do compose; o {@code @Transactional} desfaz tudo). */
@SpringBootTest
@Transactional
class ServicoRepositorySimilaridadeTest {

    @Autowired ServicoRepository servicoRepository;

    private void servico(String texto, String codigo) {
        Servico s = new Servico();
        s.setAreaServico("Teste");
        s.setTextoBreve(texto);
        s.setAbreviatura("TST");
        s.setComplemento("_TESTE");
        s.setId1Condicao("Hora normal");
        s.setId2Cnae("1234567");
        s.setCodigoPadronizado(codigo);
        servicoRepository.saveAndFlush(s);
    }

    @Test
    void devolveSoOQueAtingeOMinimo_ordenadoPeloMaiorScore() {
        servico("Zqxjv ploktrum vandelio", "TST_SIM1_HN1234567");
        servico("Zqxjv ploktrum vandelio especial", "TST_SIM2_HN1234567");
        servico("Wholly unrelated vyxzq", "TST_SIM3_HN1234567");

        List<ServicoSimilar> r = servicoRepository.buscarSimilares("zqxjv ploktrum vandelio", 0.4, 5);

        assertThat(r).extracting(ServicoSimilar::getCodigoPadronizado)
                .containsExactly("TST_SIM1_HN1234567", "TST_SIM2_HN1234567");
        assertThat(r.get(0).getScore()).isEqualTo(1.0);
        assertThat(r).allMatch(s -> s.getScore() >= 0.4);
    }

    @Test
    void textoSemParecidoDevolveVazio() {
        servico("Zqxjv ploktrum vandelio", "TST_SIM4_HN1234567");

        assertThat(servicoRepository.buscarSimilares("nada a ver kkkkkk", 0.4, 5)).isEmpty();
    }
}
