package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoBuscaNbs.Tipo;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.SugestaoNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.service.BuscaNbsService;
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

@WebMvcTest(BuscaNbsController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "APROVADOR")
class BuscaNbsControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BuscaNbsService service;

    @Test
    void buscar_devolveTipoEResultados() throws Exception {
        when(service.buscar("instalacao")).thenReturn(new ResultadoBuscaNbs(Tipo.SUGESTOES,
                List.of(new SugestaoNbs("1.01", "Instalação elétrica", 0.62))));

        mvc.perform(get("/api/nbs/busca").param("texto", "instalacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SUGESTOES"))
                .andExpect(jsonPath("$.resultados[0].codigoNbs").value("1.01"))
                .andExpect(jsonPath("$.resultados[0].score").value(0.62));
    }

    @Test
    void buscar_semParametroTextoViraBadRequest() throws Exception {
        mvc.perform(get("/api/nbs/busca")).andExpect(status().isBadRequest());
    }
}
