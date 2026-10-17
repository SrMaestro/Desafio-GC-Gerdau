package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.NbsSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs.Tipo;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscaNbsServiceTest {

    @Mock
    DicionarioDeParaRepository deParaRepository;

    @Mock
    CatNbsRepository catNbsRepository;

    BuscaNbsService service;

    @BeforeEach
    void setUp() {
        service = new BuscaNbsService(deParaRepository, catNbsRepository);
    }

    private NbsSimilar similar(String codigo, String descricao, double score) {
        return new NbsSimilar() {
            public String getCodigoNbs() { return codigo; }
            public String getDescricao() { return descricao; }
            public Double getScore() { return score; }
        };
    }

    @Test
    void buscar_textoCurtoNaoConsultaNada() {
        ResultadoBuscaNbs r = service.buscar(" a ");

        assertEquals(Tipo.SUGESTOES, r.tipo());
        assertTrue(r.resultados().isEmpty());
        verify(deParaRepository, never()).findByNomeNormalizadoAndStatus(anyString(), org.mockito.ArgumentMatchers.any());
        verify(catNbsRepository, never()).buscarSugestoes(anyString(), anyDouble(), anyInt());
    }

    @Test
    void buscar_devolveMapeamentoAprovadoComoExatoSemBuscaFuzzy() {
        DicionarioDePara aprovado = new DicionarioDePara();
        aprovado.setCodigoNbs("1.01");
        CatNbs nbs = new CatNbs();
        nbs.setCodigoNbs("1.01");
        nbs.setDescricaoAtividade("Instalação elétrica");
        when(deParaRepository.findByNomeNormalizadoAndStatus("instalacao eletrica", StatusDePara.APROVADO))
                .thenReturn(Optional.of(aprovado));
        when(catNbsRepository.findById("1.01")).thenReturn(Optional.of(nbs));

        ResultadoBuscaNbs r = service.buscar("  Instalação ELÉTRICA ");

        assertEquals(Tipo.EXATO, r.tipo());
        assertEquals("1.01", r.resultados().get(0).codigoNbs());
        assertEquals("Instalação elétrica", r.resultados().get(0).descricao());
        verify(catNbsRepository, never()).buscarSugestoes(anyString(), anyDouble(), anyInt());
    }

    @Test
    void buscar_semAprovadoFazBuscaFuzzyNoCatalogo() {
        when(deParaRepository.findByNomeNormalizadoAndStatus("instalaa eletrica", StatusDePara.APROVADO))
                .thenReturn(Optional.empty());
        when(catNbsRepository.buscarSugestoes("instalaa eletrica", BuscaNbsService.LIMIAR_SUGESTAO,
                BuscaNbsService.MAX_SUGESTOES))
                .thenReturn(List.of(similar("1.01", "Instalação elétrica", 0.62)));

        ResultadoBuscaNbs r = service.buscar("instalaa eletrica");

        assertEquals(Tipo.SUGESTOES, r.tipo());
        assertEquals(1, r.resultados().size());
        assertEquals(0.62, r.resultados().get(0).score(), 1e-9);
    }

    @Test
    void buscar_semNadaParecidoDevolveListaVazia() {
        when(deParaRepository.findByNomeNormalizadoAndStatus("xyz", StatusDePara.APROVADO))
                .thenReturn(Optional.empty());
        when(catNbsRepository.buscarSugestoes("xyz", BuscaNbsService.LIMIAR_SUGESTAO, BuscaNbsService.MAX_SUGESTOES))
                .thenReturn(List.of());

        assertTrue(service.buscar("xyz").resultados().isEmpty());
    }
}
