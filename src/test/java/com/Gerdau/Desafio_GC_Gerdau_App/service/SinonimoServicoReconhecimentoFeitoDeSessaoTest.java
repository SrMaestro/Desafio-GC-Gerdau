package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.SinonimoServicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Com {@code open-in-view=false} não há sessão fora do serviço: o serviço devolvido por {@code reconhecer} tem que
 * vir carregado, ou o cadastro e a importação estouram com LazyInitializationException ao ler o código dele.
 * Grava de verdade (e desfaz no {@code finally}), por isso não é {@code @Transactional}; usa o Postgres do compose.
 */
@SpringBootTest
class SinonimoServicoReconhecimentoFeitoDeSessaoTest {

    @Autowired SinonimoServicoService sinonimoService;
    @Autowired ServicoRepository servicoRepository;
    @Autowired SinonimoServicoRepository sinonimoRepository;
    @Autowired TransactionTemplate tx;

    @Test
    void servicoReconhecidoPodeSerLidoForaDeTransacao() {
        Servico s = new Servico();
        s.setAreaServico("Teste");
        s.setTextoBreve("Texto do servico de teste do sinonimo");
        s.setAbreviatura("TSTSIN");
        s.setComplemento("_TSTSIN");
        s.setId1Condicao("Hora normal");
        s.setId2Cnae("7654321");
        s.setCodigoPadronizado("TSTSIN_TSTSIN_HN7654321");
        Long sinonimoId = null;
        Servico salvo = null;
        try {
            salvo = servicoRepository.saveAndFlush(s);
            sinonimoId = sinonimoService.registrar("Texto sujo do sinonimo de teste", "Hora normal", "7654321",
                    salvo, "teste", null).getId();

            Servico reconhecido = sinonimoService
                    .reconhecer("texto sujo do sinonimo de teste", "Hora normal", "7654321").orElseThrow();

            assertThat(reconhecido.getCodigoPadronizado()).isEqualTo("TSTSIN_TSTSIN_HN7654321");
            assertThat(reconhecido.getTextoBreve()).isEqualTo("Texto do servico de teste do sinonimo");
        } finally {
            Long idSinonimo = sinonimoId;
            Servico servico = salvo;
            tx.executeWithoutResult(st -> {
                if (idSinonimo != null) sinonimoRepository.deleteById(idSinonimo);
                if (servico != null) servicoRepository.deleteById(servico.getId());
            });
        }
    }
}
