package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.ConsultaCadastro;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SugestaoConsulta;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Usa o Postgres do compose; o {@code @Transactional} desfaz tudo. */
@SpringBootTest
@Transactional
class ConsultaCadastroRepositoryTest {

    @Autowired ConsultaCadastroRepository repository;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;

    private ConsultaCadastro consulta(String texto, ResultadoConsulta resultado) {
        ConsultaCadastro c = new ConsultaCadastro();
        c.setUsuario("teste");
        c.setTextoBreve(texto);
        c.setResultado(resultado);
        return c;
    }

    private SugestaoConsulta sugestao(ConsultaCadastro c, int servicoId, double score) {
        SugestaoConsulta s = new SugestaoConsulta();
        s.setConsulta(c);
        s.setServicoId(servicoId);
        s.setCodigoPadronizado("COD" + servicoId);
        s.setTextoBreve("Texto " + servicoId);
        s.setScore(score);
        return s;
    }

    @Test
    void salvaConsultaComSugestoesEBuscaComSugestoesCarregadas() {
        ConsultaCadastro c = consulta("Solda de tubo", ResultadoConsulta.BLOQUEADO);
        c.getSugestoes().add(sugestao(c, 1, 0.5));
        c.getSugestoes().add(sugestao(c, 2, 0.9));
        Long id = repository.saveAndFlush(c).getId();
        em.clear();

        ConsultaCadastro achada = repository.findComSugestoesById(id).orElseThrow();

        assertThat(emf.getPersistenceUnitUtil().isLoaded(achada, "sugestoes")).isTrue();
        assertThat(achada.getSugestoes()).extracting(SugestaoConsulta::getScore).containsExactly(0.9, 0.5);
        assertThat(achada.getCriadoEm()).isNotNull();
    }

    @Test
    void listaAsMaisRecentesPrimeiro() {
        Long antiga = repository.saveAndFlush(consulta("antiga", ResultadoConsulta.CADASTRADO)).getId();
        Long recente = repository.saveAndFlush(consulta("recente", ResultadoConsulta.CADASTRADO)).getId();
        em.createNativeQuery("UPDATE consultas_cadastro SET criado_em = TIMESTAMP '2020-01-01 00:00:00' WHERE id = ?1")
                .setParameter(1, antiga).executeUpdate();
        em.clear();

        List<ConsultaCadastro> lista = repository.findTop200ByOrderByCriadoEmDesc();

        assertThat(lista).extracting(ConsultaCadastro::getId).containsSubsequence(recente, antiga);
    }

    @Test
    void rejeitaResultadoForaDoCatalogo() {
        assertThatThrownBy(() -> em.createNativeQuery(
                "INSERT INTO consultas_cadastro (usuario, texto_breve, resultado) VALUES ('t', 'x', 'XYZ')")
                .executeUpdate()).isInstanceOf(RuntimeException.class);
    }
}
