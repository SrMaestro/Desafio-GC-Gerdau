package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.PainelMonitoramento;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Usa o Postgres do compose; o {@code @Transactional} desfaz tudo, inclusive o retrato da rotina semanal. */
@SpringBootTest
@Transactional
class MonitoramentoServiceTest {

    @Autowired MonitoramentoService service;
    @Autowired ServicoRepository repository;
    @Autowired JdbcTemplate jdbc;

    private void salvar(String texto, String codigo) {
        Servico s = new Servico();
        s.setAreaServico("Teste");
        s.setTextoBreve(texto);
        s.setAbreviatura("MON");
        s.setComplemento("");
        s.setId1Condicao("Hora normal");
        s.setId2Cnae("9999999");
        s.setCodigoPadronizado(codigo);
        repository.saveAndFlush(s);
    }

    @Test
    void detectaParParecidoComMesmaCondicaoECnae() {
        salvar("Manutencao preventiva de esteira transportadora", "MON_A_HN9999999");
        salvar("Manutencao preventiva da esteira transportadora", "MON_B_HN9999999");
        assertThat(service.duplicatasPossiveis())
                .anyMatch(d -> d.codigoA().equals("MON_A_HN9999999") && d.codigoB().equals("MON_B_HN9999999")
                        && d.similaridade() >= 0.8 && !d.legado());
    }

    @Test
    void painelMonta() {
        PainelMonitoramento p = service.painel();
        assertThat(p.duplicatasPossiveis()).isEqualTo(p.duplicatas().size());
        assertThat(p.servicosSemNbs()).isNotNegative();
    }

    @Test
    void rotinaSemanalGravaRetrato() {
        long antes = jdbc.queryForObject("SELECT count(*) FROM monitoramento_execucao", Long.class);
        service.rotinaSemanal();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM monitoramento_execucao", Long.class)).isEqualTo(antes + 1);
    }
}
