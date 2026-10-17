package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoController.class)
class ServicoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ServicoService service;

    @MockitoBean
    ServicoRepository repository;

    @Test
    void similares_devolveOsServicosParecidos() throws Exception {
        when(service.similares("concretagem")).thenReturn(List.of(
                new SimilarDto(1, "Concretagem de fundação", "CONC_CONCRE_HN4399103", 0.72)));

        mvc.perform(get("/servicos/similares").param("texto", "concretagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigoPadronizado").value("CONC_CONCRE_HN4399103"))
                .andExpect(jsonPath("$[0].score").value(0.72));
    }
}
