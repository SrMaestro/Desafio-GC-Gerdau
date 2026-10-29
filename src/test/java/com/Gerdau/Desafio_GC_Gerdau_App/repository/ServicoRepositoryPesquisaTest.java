package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prende o contrato de {@code pesquisar}. Usa o Postgres do compose, mas o {@code @Transactional} desfaz tudo;
 * confere só as linhas de uma área inventada aqui, para não depender dos dados do banco.
 */
@SpringBootTest
@Transactional
class ServicoRepositoryPesquisaTest {

    private static final String AREA = "Zqx Área Pesquisa";

    @Autowired ServicoRepository repository;

    private void servico(String texto, String codigo) {
        Servico s = new Servico();
        s.setAreaServico(AREA);
        s.setTextoBreve(texto);
        s.setAbreviatura("PSQ");
        s.setComplemento("_TESTE");
        s.setId1Condicao("Hora normal");
        s.setId2Cnae("7654321");
        s.setCodigoPadronizado(codigo);
        repository.saveAndFlush(s);
    }

    private void tresServicos() {
        servico("Manutenção de bomba", "PSQ_A_HN7654321");
        servico("Pintura de tanque", "PSQ_B_HN7654321");
        servico("Inspeção de válvula", "PSQ_C_HN7654321");
    }

    @Test
    void semTermoNemAreaListaTudoEmPaginasPelaOrdemDoId() {
        long total = repository.count();

        Page<Servico> pagina = repository.pesquisar("", "", PageRequest.of(0, 5));

        assertThat(pagina.getTotalElements()).isEqualTo(total);
        assertThat(pagina.getContent()).hasSizeLessThanOrEqualTo(5);
        assertThat(pagina.getContent()).extracting(Servico::getId).isSorted();
    }

    @Test
    void pagina_devolveSoOPedaco_eContaOTotalDaArea() {
        tresServicos();

        Page<Servico> p0 = repository.pesquisar("", AREA, PageRequest.of(0, 2));
        Page<Servico> p1 = repository.pesquisar("", AREA, PageRequest.of(1, 2));

        assertThat(p0.getTotalElements()).isEqualTo(3);
        assertThat(p0.getTotalPages()).isEqualTo(2);
        assertThat(p0.getContent()).extracting(Servico::getCodigoPadronizado)
                .containsExactly("PSQ_A_HN7654321", "PSQ_B_HN7654321");
        assertThat(p1.getContent()).extracting(Servico::getCodigoPadronizado).containsExactly("PSQ_C_HN7654321");
    }

    @Test
    void termo_ignoraMaiusculasEAcentosDoTexto() {
        tresServicos();

        Page<Servico> achou = repository.pesquisar("manutencao", AREA, PageRequest.of(0, 10));
        Page<Servico> achouComAcento = repository.pesquisar("valvula", AREA, PageRequest.of(0, 10));

        assertThat(achou.getContent()).extracting(Servico::getCodigoPadronizado).containsExactly("PSQ_A_HN7654321");
        assertThat(achouComAcento.getContent()).extracting(Servico::getCodigoPadronizado)
                .containsExactly("PSQ_C_HN7654321");
    }

    @Test
    void termo_procuraNoCodigoNoCnaeETambemNoIdDoServico() {
        tresServicos();
        Integer id = repository.pesquisar("pintura", AREA, PageRequest.of(0, 10)).getContent().get(0).getId();

        assertThat(repository.pesquisar("psq_b", AREA, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
        assertThat(repository.pesquisar("7654321", AREA, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(3);
        assertThat(repository.pesquisar(String.valueOf(id), AREA, PageRequest.of(0, 10)).getContent())
                .extracting(Servico::getId).contains(id);
    }

    @Test
    void termo_comPercentEUnderlineEhTextoLiteral() {
        tresServicos();

        assertThat(repository.pesquisar("%", AREA, PageRequest.of(0, 10)).getTotalElements()).isZero();
        assertThat(repository.pesquisar("psq_", AREA, PageRequest.of(0, 10)).getTotalElements()).isEqualTo(3);
        assertThat(repository.pesquisar("p_q", AREA, PageRequest.of(0, 10)).getTotalElements()).isZero();
    }

    @Test
    void areaFiltraEoTermoSoOlhaDentroDela() {
        tresServicos();

        assertThat(repository.pesquisar("bomba", "Outra área que não existe", PageRequest.of(0, 10))
                .getTotalElements()).isZero();
    }
}
