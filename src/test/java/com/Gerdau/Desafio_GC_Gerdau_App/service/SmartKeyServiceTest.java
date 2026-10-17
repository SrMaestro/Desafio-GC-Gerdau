package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.SmartKey;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartKeyServiceTest {

    @Mock
    CatNbsRepository catNbsRepository;

    @Mock
    CatHorarioRepository catHorarioRepository;

    SmartKeyService service;

    @BeforeEach
    void setUp() {
        service = new SmartKeyService(catNbsRepository, catHorarioRepository);
    }

    private CatNbs nbs() {
        CatNbs n = new CatNbs();
        n.setCodigoNbs("1.0106.19.00");
        n.setDescricaoAtividade("Serviços de instalação elétrica");
        return n;
    }

    private CatHorario horario() {
        CatHorario h = new CatHorario();
        h.setSufixoCodigo("HN");
        h.setDescricaoHorario("Hora normal");
        return h;
    }

    @Test
    void gerar_juntaNbsESufixoComHifen() {
        when(catNbsRepository.findById("1.0106.19.00")).thenReturn(Optional.of(nbs()));
        when(catHorarioRepository.findBySufixoCodigo("HN")).thenReturn(Optional.of(horario()));

        SmartKey chave = service.gerar("1.0106.19.00", "HN");

        assertEquals("1.0106.19.00-HN", chave.chave());
        assertEquals("Hora normal", chave.descricaoHorario());
        assertEquals("Serviços de instalação elétrica", chave.descricaoNbs());
    }

    @Test
    void gerar_aceitaSufixoEmMinusculaEComEspacos() {
        when(catNbsRepository.findById("1.0106.19.00")).thenReturn(Optional.of(nbs()));
        when(catHorarioRepository.findBySufixoCodigo("HN")).thenReturn(Optional.of(horario()));

        assertEquals("1.0106.19.00-HN", service.gerar(" 1.0106.19.00 ", " hn ").chave());
    }

    @Test
    void gerar_recusaNbsDesconhecido() {
        when(catNbsRepository.findById("9.9999.99.99")).thenReturn(Optional.empty());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.gerar("9.9999.99.99", "HN"));
        assertTrue(e.getMessage().contains("9.9999.99.99"));
    }

    @Test
    void gerar_recusaHorarioDesconhecido() {
        when(catNbsRepository.findById("1.0106.19.00")).thenReturn(Optional.of(nbs()));
        when(catHorarioRepository.findBySufixoCodigo("XX")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.gerar("1.0106.19.00", "xx"));
    }

    @Test
    void gerar_exigeNbsEHorario() {
        assertThrows(IllegalArgumentException.class, () -> service.gerar(" ", "HN"));
        assertThrows(IllegalArgumentException.class, () -> service.gerar("1.0106.19.00", null));
    }
}
