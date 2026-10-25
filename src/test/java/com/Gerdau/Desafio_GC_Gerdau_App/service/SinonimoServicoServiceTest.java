package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SinonimoServico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.SinonimoServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SinonimoServicoServiceTest {

    @Mock
    SinonimoServicoRepository repository;

    SinonimoServicoService service;

    @BeforeEach
    void setUp() {
        service = new SinonimoServicoService(repository);
    }

    @Test
    void reconhece_textoComAcentoECaixaDiferentes() {
        Servico servico = new Servico();
        SinonimoServico sinonimo = new SinonimoServico();
        sinonimo.setServico(servico);
        when(repository.findByTextoNormalizadoAndCondicaoAndCnae("manut. motor eletrico", "Hora normal", "4399103"))
                .thenReturn(Optional.of(sinonimo));

        Optional<Servico> achado = service.reconhecer("manut. motor eletrico", "Hora normal", "4399103");
        assertSame(servico, achado.orElseThrow());
        assertTrue(service.reconhecer("Manut. MOTOR elétrico", "Hora normal", "4399103").isPresent());
    }

    @Test
    void naoReconhece_seCondicaoOuCnaeDiferem() {
        when(repository.findByTextoNormalizadoAndCondicaoAndCnae("manut. motor eletrico", "Hora extra", "4399103"))
                .thenReturn(Optional.empty());
        when(repository.findByTextoNormalizadoAndCondicaoAndCnae("manut. motor eletrico", "Hora normal", "1111111"))
                .thenReturn(Optional.empty());

        assertTrue(service.reconhecer("Manut. MOTOR elétrico", "Hora extra", "4399103").isEmpty());
        assertTrue(service.reconhecer("Manut. MOTOR elétrico", "Hora normal", "1111111").isEmpty());
    }

    @Test
    void registrar_gravaTextoNormalizado() {
        when(repository.save(org.mockito.ArgumentMatchers.any(SinonimoServico.class))).thenAnswer(i -> i.getArgument(0));
        Servico servico = new Servico();

        service.registrar("Manut. MOTOR elétrico", "Hora normal", "4399103", servico, "ana", 7L);

        ArgumentCaptor<SinonimoServico> c = ArgumentCaptor.forClass(SinonimoServico.class);
        verify(repository).save(c.capture());
        SinonimoServico s = c.getValue();
        assertEquals("manut. motor eletrico", s.getTextoNormalizado());
        assertEquals("Manut. MOTOR elétrico", s.getTextoOriginal());
        assertEquals("Hora normal", s.getCondicao());
        assertEquals("4399103", s.getCnae());
        assertSame(servico, s.getServico());
        assertEquals("ana", s.getAprovadoPor());
        assertEquals(7L, s.getLinhaId());
    }
}
