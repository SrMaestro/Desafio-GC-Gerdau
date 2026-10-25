package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SinonimoServico;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Com {@code open-in-view=false} o serviço do sinônimo precisa vir carregado junto, senão ler o código dele fora
 * da transação estoura LazyInitializationException. Usa o Postgres do compose; o {@code @Transactional} desfaz tudo.
 */
@SpringBootTest
@Transactional
class SinonimoServicoRepositoryTest {

    @Autowired ServicoRepository servicoRepository;
    @Autowired SinonimoServicoRepository sinonimoRepository;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;

    @Test
    void buscaPorChaveTrazOServicoCarregado() {
        Servico s = new Servico();
        s.setAreaServico("Teste");
        s.setTextoBreve("Texto do servico de teste do sinonimo");
        s.setAbreviatura("TSTSIN");
        s.setComplemento("_TSTSIN");
        s.setId1Condicao("Hora normal");
        s.setId2Cnae("7654321");
        s.setCodigoPadronizado("TSTSIN_TSTSIN_HN7654321");
        servicoRepository.saveAndFlush(s);
        SinonimoServico sin = new SinonimoServico();
        sin.setTextoNormalizado("texto sujo do sinonimo de teste");
        sin.setTextoOriginal("Texto sujo do sinonimo de teste");
        sin.setCondicao("Hora normal");
        sin.setCnae("7654321");
        sin.setServico(s);
        sin.setAprovadoPor("teste");
        sinonimoRepository.saveAndFlush(sin);
        em.clear(); // como numa nova requisição: nada em cache de sessão

        SinonimoServico achado = sinonimoRepository
                .findByTextoNormalizadoAndCondicaoAndCnae("texto sujo do sinonimo de teste", "Hora normal", "7654321")
                .orElseThrow();

        assertThat(emf.getPersistenceUnitUtil().isLoaded(achado, "servico")).isTrue();
        assertThat(org.hibernate.Hibernate.isInitialized(achado.getServico())).isTrue();
    }
}
