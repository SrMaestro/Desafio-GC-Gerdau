package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prende o contrato de {@code buscarSimilares}. Usa o Postgres do compose, mas o {@code @Transactional} desfaz tudo;
 * confere só as linhas inseridas aqui (textos sem sentido), para não depender dos dados do banco.
 * As similaridades abaixo em relação a "zqxjv ploktrum vandelio" foram medidas com {@code similarity()}.
 */
@SpringBootTest
@Transactional
class ServicoRepositorySimilaridadeTest {

    private static final String BASE = "zqxjv ploktrum vandelio";
    private static final String RESTO = " ghbnm rtyuw cvfd poiu lkjh mnbv asdf qwer";

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

    private List<String> codigos(String texto) {
        return servicoRepository.buscarSimilares(texto, 0.4, 500).stream()
                .map(ServicoSimilar::getCodigoPadronizado).toList();
    }

    @Test
    void textoIgualVemPrimeiroComScoreUm_eMaisParecidoAntesDoMenos() {
        servico("Zqxjv ploktrum vandelio", "TST_SIM1_HN1234567");
        servico("Zqxjv ploktrum vandelio ghbnm rtyuw", "TST_SIM2_HN1234567"); // ~0,67

        List<ServicoSimilar> r = servicoRepository.buscarSimilares(BASE, 0.4, 500);

        assertThat(r.get(0).getCodigoPadronizado()).isEqualTo("TST_SIM1_HN1234567");
        assertThat(r.get(0).getScore()).isEqualTo(1.0);
        assertThat(r).extracting(ServicoSimilar::getCodigoPadronizado)
                .containsSubsequence("TST_SIM1_HN1234567", "TST_SIM2_HN1234567");
    }

    @Test
    void abaixoDoMinimoFica_eAcimaEntra() {
        servico(BASE + RESTO + " zxcv", "TST_SIM3_HN1234567"); // ~0,35: excluído com mínimo 0,4
        servico(BASE + " ghbnm rtyuw cvfd poiu lkjh mnbv", "TST_SIM4_HN1234567"); // ~0,44: incluído

        List<String> r = codigos(BASE);

        assertThat(r).contains("TST_SIM4_HN1234567").doesNotContain("TST_SIM3_HN1234567");
    }

    @Test
    void textoSemParecidoNaoTrazOsInseridos() {
        servico(BASE, "TST_SIM5_HN1234567");

        assertThat(codigos("nada a ver kkkkkk")).doesNotContain("TST_SIM5_HN1234567");
    }
}
