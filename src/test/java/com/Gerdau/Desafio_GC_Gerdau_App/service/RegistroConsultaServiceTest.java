package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ConsultaCadastro;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ConsultaCadastroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistroConsultaServiceTest {

    @Mock
    ConsultaCadastroRepository repository;

    @InjectMocks
    RegistroConsultaService service;

    @Test
    void registrar_montaConsultaComSugestoes() {
        service.registrar("ana", "Solda de tubo", "Hora normal", "4399103", "SOLDA_HN4399103",
                ResultadoConsulta.REQUER_CONFIRMACAO, null, null,
                List.of(new SimilarDto(7, "Soldagem de tubos", "SOLTUB_HN4399103", 0.62)), null);

        ArgumentCaptor<ConsultaCadastro> captor = ArgumentCaptor.forClass(ConsultaCadastro.class);
        verify(repository).save(captor.capture());
        ConsultaCadastro c = captor.getValue();
        assertEquals("ana", c.getUsuario());
        assertEquals("Solda de tubo", c.getTextoBreve());
        assertEquals("Hora normal", c.getCondicao());
        assertEquals("4399103", c.getCnae());
        assertEquals("SOLDA_HN4399103", c.getCodigoGerado());
        assertEquals(ResultadoConsulta.REQUER_CONFIRMACAO, c.getResultado());
        assertEquals(1, c.getSugestoes().size());
        var s = c.getSugestoes().get(0);
        assertEquals(7, s.getServicoId());
        assertEquals("SOLTUB_HN4399103", s.getCodigoPadronizado());
        assertEquals("Soldagem de tubos", s.getTextoBreve());
        assertEquals(0.62, s.getScore());
        assertSame(c, s.getConsulta());
    }

    @Test
    void registrar_gravaDecisaoEServico() {
        service.registrar("ana", "Solda", null, null, null,
                ResultadoConsulta.SUGERIDO, DecisaoConsulta.REUTILIZOU_EXISTENTE, 7, List.of(), null);

        ArgumentCaptor<ConsultaCadastro> captor = ArgumentCaptor.forClass(ConsultaCadastro.class);
        verify(repository).save(captor.capture());
        assertEquals(DecisaoConsulta.REUTILIZOU_EXISTENTE, captor.getValue().getDecisao());
        assertEquals(7, captor.getValue().getServicoId());
    }

    @Test
    void registrar_comSugestoesNulaOuVazia_naoQuebra() {
        service.registrar("ana", "a", null, null, null, ResultadoConsulta.CADASTRADO, null, null, null, null);
        service.registrar("ana", "b", null, null, null, ResultadoConsulta.CADASTRADO, null, null, List.of(), null);

        ArgumentCaptor<ConsultaCadastro> captor = ArgumentCaptor.forClass(ConsultaCadastro.class);
        verify(repository, org.mockito.Mockito.times(2)).save(captor.capture());
        captor.getAllValues().forEach(c -> assertTrue(c.getSugestoes().isEmpty()));
    }

    @Test
    void registrar_quandoOBancoFalha_naoPropagaExcecao() {
        when(repository.save(any())).thenThrow(new DataAccessResourceFailureException("banco fora"));

        assertDoesNotThrow(() -> service.registrar("ana", "x", null, null, null,
                ResultadoConsulta.BLOQUEADO, null, null, List.of(), null));
    }

    @Test
    void registrar_gravaAJustificativaDaContestacao() {
        service.registrar("ana", "Bomba 20 cv", "Hora normal", "3314801", "BOMBA_HN3314801",
                ResultadoConsulta.CADASTRADO, DecisaoConsulta.CONTESTOU_BLOQUEIO, 9, List.of(),
                "Capacidade diferente");

        ArgumentCaptor<ConsultaCadastro> captor = ArgumentCaptor.forClass(ConsultaCadastro.class);
        verify(repository).save(captor.capture());
        assertEquals(DecisaoConsulta.CONTESTOU_BLOQUEIO, captor.getValue().getDecisao());
        assertEquals("Capacidade diferente", captor.getValue().getJustificativa());
    }
}
