package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Condicao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    ServicoRepository repository;

    ServicoService service;

    @BeforeEach
    void setUp() {
        service = new ServicoService(repository);
    }

    private NovoServicoForm form() {
        NovoServicoForm f = new NovoServicoForm();
        f.setAreaServico("Civil");
        f.setTextoBreve("Concretagem de fundação");
        f.setAbreviatura("conc");
        f.setComplemento("_CONCRE");
        f.setCondicao(Condicao.HORA_NORMAL);
        f.setCnae("4399103");
        return f;
    }

    private ServicoSimilar similar(double score, String condicao, String cnae) {
        return new ServicoSimilar() {
            public Integer getId() { return 1; }
            public String getTextoBreve() { return "Concretagem de fundação"; }
            public String getCodigoPadronizado() { return "CONC_CONCRE_HN4399103"; }
            public String getId1Condicao() { return condicao; }
            public String getId2Cnae() { return cnae; }
            public Double getScore() { return score; }
        };
    }

    @Test
    void geraCodigoNoPadraoDoCatalogo() {
        assertEquals("CONC_CONCRE_HN4399103", service.gerarCodigo(form()));
    }

    @Test
    void bloqueiaQuandoCodigoJaExiste() {
        when(repository.findByCodigoPadronizado("CONC_CONCRE_HN4399103")).thenReturn(Optional.of(new Servico()));
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        AnaliseSimilaridade analise = service.analisar(form());

        assertTrue(analise.codigoExistente());
        assertTrue(analise.bloqueado());
    }

    @Test
    void bloqueiaTextoQuaseIdenticoComMesmaCondicaoECnae() {
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.9, "Hora normal", "4399103")));

        assertTrue(service.analisar(form()).bloqueado());
    }

    @Test
    void textoIdenticoComOutraCondicaoSoPedeConfirmacao() {
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.95, "Hora extra Seg a Sab", "4399103")));

        AnaliseSimilaridade analise = service.analisar(form());

        assertFalse(analise.bloqueado());
        assertTrue(analise.requerConfirmacao());
    }

    @Test
    void cadastrarSemConfirmarComParecidosEhRejeitado() {
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(form(), false));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrarComConfirmacaoSalvaComCodigoGerado() {
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

        Servico salvo = service.cadastrar(form(), true);

        assertEquals("CONC_CONCRE_HN4399103", salvo.getCodigoPadronizado());
        assertEquals("Hora normal", salvo.getId1Condicao());
        assertEquals("CONC", salvo.getAbreviatura());
    }

    @Test
    void rejeitaCodigoMaiorQue40Caracteres() {
        NovoServicoForm f = form();
        f.setAbreviatura("ABCDEFGHIJ");
        f.setComplemento("_ABCDEFGHIJKLMNOPQRS");
        f.setCondicao(Condicao.ADICIONAL_PERICULOSIDADE_SEM_HE);
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false));
    }
}
