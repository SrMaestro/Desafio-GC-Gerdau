package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.FiltroAprovacao;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confere as fronteiras de transação da aprovação sem gravar nada: a unidade por linha passa pelo proxy com
 * {@code REQUIRES_NEW} e os orquestradores não abrem transação (senão um conflito desfaria as outras linhas).
 * Sobe o contexto, então precisa do Postgres do compose.
 */
@SpringBootTest
class AprovacaoImportacaoTransacaoTest {

    @Autowired AprovadorLinha aprovadorLinha;
    @Autowired TransactionAttributeSource transactionAttributeSource;

    @Test
    void cadaLinhaTemTransacaoPropria_eOsOrquestradoresNaoTem() throws Exception {
        assertThat(AopUtils.isAopProxy(aprovadorLinha)).isTrue();
        for (String metodo : new String[] {"aprovar", "registrarConflito", "rejeitar"}) {
            Class<?>[] params = metodo.equals("aprovar")
                    ? new Class<?>[] {Long.class, String.class}
                    : new Class<?>[] {Long.class, String.class, String.class};
            TransactionAttribute attr = transactionAttributeSource.getTransactionAttribute(
                    AprovadorLinha.class.getMethod(metodo, params), AprovadorLinha.class);
            assertThat(attr).as(metodo).isNotNull();
            assertThat(attr.getPropagationBehavior()).as(metodo)
                    .isEqualTo(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        }

        Class<?> s = AprovacaoImportacaoService.class;
        assertThat(transactionAttributeSource.getTransactionAttribute(
                s.getMethod("aprovar", Long.class, String.class), s)).isNull();
        assertThat(transactionAttributeSource.getTransactionAttribute(
                s.getMethod("rejeitar", Long.class, String.class, String.class), s)).isNull();
        assertThat(transactionAttributeSource.getTransactionAttribute(
                s.getMethod("aprovarEmBloco", Long.class, FiltroAprovacao.class, String.class), s)).isNull();
        assertThat(transactionAttributeSource.getTransactionAttribute(
                s.getMethod("rejeitarEmBloco", Long.class, FiltroAprovacao.class, String.class, String.class), s))
                .isNull();
    }
}
