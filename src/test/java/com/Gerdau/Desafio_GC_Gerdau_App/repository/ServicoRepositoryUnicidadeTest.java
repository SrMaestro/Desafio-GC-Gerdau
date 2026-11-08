package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** V15: texto normalizado gerado pelo banco e único por condição e CNAE. Usa o Postgres do compose; desfeito ao fim. */
@SpringBootTest
@Transactional
class ServicoRepositoryUnicidadeTest {

    @Autowired ServicoRepository repository;
    @Autowired EntityManager em;

    private Servico servico(String texto, String codigo, String condicao, String cnae) {
        Servico s = new Servico();
        s.setAreaServico("Teste");
        s.setTextoBreve(texto);
        s.setAbreviatura("UNQ");
        s.setComplemento("");
        s.setId1Condicao(condicao);
        s.setId2Cnae(cnae);
        s.setCodigoPadronizado(codigo);
        return s;
    }

    @Test
    void textoNormalizadoVemDoBanco() {
        Servico s = repository.saveAndFlush(servico("Manutenção Elétrica Ünica", "UNQ_A_HN1111111", "Hora normal", "1111111"));
        em.refresh(s);
        assertThat(s.getTextoNormalizado()).isEqualTo("manutencao eletrica unica");
    }

    @Test
    void bancoRejeitaMesmoTextoNormalizadoComMesmaCondicaoECnae() {
        repository.saveAndFlush(servico("Pintura industrial", "UNQ_B_HN2222222", "Hora normal", "2222222"));
        assertThatThrownBy(() -> repository.saveAndFlush(
                servico("PINTURA INDUSTRIAL", "UNQ_C_HN2222222", "Hora normal", "2222222")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void mesmoTextoEmOutraCondicaoOuCnaeContinuaPermitido() {
        repository.saveAndFlush(servico("Soldagem de tubulação", "UNQ_D_HN3333333", "Hora normal", "3333333"));
        repository.saveAndFlush(servico("Soldagem de tubulacao", "UNQ_E_HE3333333", "Hora extra Seg a Sab", "3333333"));
        repository.saveAndFlush(servico("Soldagem de tubulacao", "UNQ_F_HN4444444", "Hora normal", "4444444"));
    }
}
