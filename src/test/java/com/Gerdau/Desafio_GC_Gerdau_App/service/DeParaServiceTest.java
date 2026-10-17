package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDeParaHistorico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaHistoricoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.DicionarioDeParaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeParaServiceTest {

    @Mock
    DicionarioDeParaRepository repository;

    @Mock
    DicionarioDeParaHistoricoRepository historicoRepository;

    @Mock
    CatNbsRepository catNbsRepository;

    DeParaService service;

    @BeforeEach
    void setUp() {
        service = new DeParaService(repository, historicoRepository, catNbsRepository);
    }

    private DicionarioDePara mapeamento(long id, String nome, StatusDePara status) {
        DicionarioDePara m = new DicionarioDePara();
        m.setId(id);
        m.setNomeNormalizado(nome);
        m.setNomeOriginal(nome);
        m.setStatus(status);
        return m;
    }

    @Test
    void normalizar_removeAcentoCaixaEEspacosExtras() {
        assertEquals("manutencao eletrica", DeParaService.normalizar("  Manutenção   ELÉTRICA "));
    }

    @Test
    void confirmar_registraPendenteComNomeNormalizado() {
        when(catNbsRepository.existsById("1.01")).thenReturn(true);
        when(repository.findByNomeNormalizadoAndStatus("instalacao eletrica", StatusDePara.APROVADO))
                .thenReturn(Optional.empty());
        when(repository.registrarConfirmacao("instalacao eletrica", "Instalação Elétrica", "1.01", 0.7f))
                .thenReturn(1);

        assertTrue(service.confirmar(" Instalação Elétrica ", "1.01", 0.7f));
    }

    @Test
    void confirmar_ignoraNomeQueJaTemMapeamentoAprovado() {
        when(catNbsRepository.existsById("1.01")).thenReturn(true);
        when(repository.findByNomeNormalizadoAndStatus("motor", StatusDePara.APROVADO))
                .thenReturn(Optional.of(mapeamento(1, "motor", StatusDePara.APROVADO)));

        assertFalse(service.confirmar("Motor", "1.01", null));
        verify(repository, never()).registrarConfirmacao(anyString(), anyString(), anyString(), any());
    }

    @Test
    void confirmar_rejeitaNbsDesconhecido() {
        when(catNbsRepository.existsById("9.99")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.confirmar("motor", "9.99", null));
    }

    @Test
    void confirmar_rejeitaNomeEmBranco() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmar("  ", "1.01", null));
    }

    @Test
    void aprovar_aprovaPendenteERejeitaOsOutrosPendentesDoMesmoNome() {
        DicionarioDePara alvo = mapeamento(1, "motor", StatusDePara.PENDENTE);
        DicionarioDePara concorrente = mapeamento(2, "motor", StatusDePara.PENDENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(alvo));
        when(repository.findByNomeNormalizadoAndStatus("motor", StatusDePara.APROVADO)).thenReturn(Optional.empty());
        when(repository.findByNomeNormalizadoAndStatusOrderByConfirmacoesDesc("motor", StatusDePara.PENDENTE))
                .thenReturn(List.of(alvo, concorrente));

        DicionarioDePara resultado = service.aprovar(1L, "ana");

        assertEquals(StatusDePara.APROVADO, resultado.getStatus());
        assertEquals("ana", resultado.getRevisadoPor());
        assertNotNull(resultado.getRevisadoEm());
        assertEquals(StatusDePara.REJEITADO, concorrente.getStatus());
        assertNotNull(concorrente.getMotivo());

        ArgumentCaptor<DicionarioDeParaHistorico> captor = ArgumentCaptor.forClass(DicionarioDeParaHistorico.class);
        verify(historicoRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals(StatusDePara.PENDENTE, captor.getAllValues().get(0).getDeStatus());
        assertEquals(StatusDePara.APROVADO, captor.getAllValues().get(0).getParaStatus());
    }

    @Test
    void aprovar_recusaQuandoJaExisteAprovadoParaOMesmoNome() {
        when(repository.findById(2L)).thenReturn(Optional.of(mapeamento(2, "motor", StatusDePara.PENDENTE)));
        when(repository.findByNomeNormalizadoAndStatus("motor", StatusDePara.APROVADO))
                .thenReturn(Optional.of(mapeamento(1, "motor", StatusDePara.APROVADO)));

        assertThrows(IllegalStateException.class, () -> service.aprovar(2L, "ana"));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void aprovar_traduzCorridaDoIndiceUnicoEmErroDeNegocio() {
        when(repository.findById(1L)).thenReturn(Optional.of(mapeamento(1, "motor", StatusDePara.PENDENTE)));
        when(repository.findByNomeNormalizadoAndStatus("motor", StatusDePara.APROVADO)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq_de_para_aprovado"));

        assertThrows(IllegalStateException.class, () -> service.aprovar(1L, "ana"));
    }

    @Test
    void aprovar_soAceitaPendente() {
        when(repository.findById(1L)).thenReturn(Optional.of(mapeamento(1, "motor", StatusDePara.REJEITADO)));

        assertThrows(IllegalStateException.class, () -> service.aprovar(1L, "ana"));
    }

    @Test
    void aprovar_exigeRevisor() {
        assertThrows(IllegalArgumentException.class, () -> service.aprovar(1L, " "));
    }

    @Test
    void rejeitar_exigeMotivoERegistraHistorico() {
        assertThrows(IllegalArgumentException.class, () -> service.rejeitar(1L, "ana", " "));

        DicionarioDePara alvo = mapeamento(1, "motor", StatusDePara.PENDENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(alvo));
        when(repository.save(alvo)).thenReturn(alvo);

        DicionarioDePara resultado = service.rejeitar(1L, "ana", "NBS errado");

        assertEquals(StatusDePara.REJEITADO, resultado.getStatus());
        assertEquals("NBS errado", resultado.getMotivo());
        verify(historicoRepository).save(any(DicionarioDeParaHistorico.class));
    }

    @Test
    void revogar_soAceitaAprovado() {
        when(repository.findById(1L)).thenReturn(Optional.of(mapeamento(1, "motor", StatusDePara.PENDENTE)));

        assertThrows(IllegalStateException.class, () -> service.revogar(1L, "ana", "motivo"));
    }

    @Test
    void revogar_marcaComoRevogado() {
        DicionarioDePara alvo = mapeamento(1, "motor", StatusDePara.APROVADO);
        when(repository.findById(1L)).thenReturn(Optional.of(alvo));
        when(repository.save(alvo)).thenReturn(alvo);

        assertEquals(StatusDePara.REVOGADO, service.revogar(1L, "ana", "NBS mudou").getStatus());
    }

    @Test
    void aprovar_mapeamentoInexistente() {
        when(repository.findById(eq(99L))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.aprovar(99L, "ana"));
    }

    @Test
    void listarAprovados_consultaPorStatusAprovado() {
        DicionarioDePara aprovado = mapeamento(1, "motor", StatusDePara.APROVADO);
        when(repository.findByStatusOrderByConfirmacoesDescCriadoEmAsc(StatusDePara.APROVADO))
                .thenReturn(List.of(aprovado));

        assertEquals(List.of(aprovado), service.listarAprovados());
    }
}
