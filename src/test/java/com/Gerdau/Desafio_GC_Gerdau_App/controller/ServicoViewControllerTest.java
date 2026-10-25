package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoViewController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "APROVADOR")
class ServicoViewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ServicoRepository repository;

    @MockitoBean
    ServicoService service;

    @MockitoBean
    CatNbsRepository catNbsRepository;

    @MockitoBean
    CatHorarioRepository catHorarioRepository;

    private CatHorario horasExtra() {
        CatHorario h = new CatHorario();
        h.setSufixoCodigo("HES");
        h.setDescricaoHorario("Hora extra Seg a Sab");
        return h;
    }

    private CatNbs nbs() {
        CatNbs n = new CatNbs();
        n.setCodigoNbs("1.0105.40.00");
        n.setDescricaoAtividade("Serviços de concretagem");
        return n;
    }

    @Test
    void novo_comNbsEHorarioPreencheOFormulario() throws Exception {
        when(repository.findAreas()).thenReturn(List.of("Civil"));
        when(catNbsRepository.findById("1.0105.40.00")).thenReturn(Optional.of(nbs()));
        when(catHorarioRepository.findByAtivoTrueOrderByIdAsc()).thenReturn(List.of(horasExtra()));
        when(catHorarioRepository.findBySufixoCodigoAndAtivoTrue("HES")).thenReturn(Optional.of(horasExtra()));

        mvc.perform(get("/ui/servicos/novo").param("nbs", "1.0105.40.00").param("horario", "HES"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Serviços de concretagem")))
                .andExpect(content().string(containsString("value=\"1.0105.40.00\"")))
                .andExpect(content().string(containsString("value=\"HES\" data-sufixo=\"HES\" selected")))
                .andExpect(content().string(containsString("id=\"previa-codigo\" data-max=\"40\"")))
                .andExpect(content().string(containsString("data-url=\"/servicos/similares\"")));
    }

    @Test
    void novo_semParametrosContinuaIgual() throws Exception {
        when(repository.findAreas()).thenReturn(List.of());

        mvc.perform(get("/ui/servicos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Código NBS:"))));
    }

    @Test
    void novo_comNbsDesconhecidoAvisaEmVezDeEsconder() throws Exception {
        when(repository.findAreas()).thenReturn(List.of());
        when(catNbsRepository.findById("9.9")).thenReturn(Optional.empty());

        mvc.perform(get("/ui/servicos/novo").param("nbs", "9.9"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Código NBS desconhecido")));
    }

    @Test
    void novo_ignoraHorarioInvalido() throws Exception {
        when(repository.findAreas()).thenReturn(List.of());

        mvc.perform(get("/ui/servicos/novo").param("horario", "ZZ"))
                .andExpect(status().isOk());
    }
}
