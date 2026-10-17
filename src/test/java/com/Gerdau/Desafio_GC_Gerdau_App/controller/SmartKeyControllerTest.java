package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.SmartKey;
import com.Gerdau.Desafio_GC_Gerdau_App.service.SmartKeyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SmartKeyController.class)
class SmartKeyControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SmartKeyService service;

    @Test
    void gerar_devolveAChaveMontada() throws Exception {
        when(service.gerar("1.0106.19.00", "HN")).thenReturn(new SmartKey("1.0106.19.00-HN", "1.0106.19.00",
                "Serviços de instalação elétrica", "HN", "Hora normal"));

        mvc.perform(get("/api/smart-key").param("nbs", "1.0106.19.00").param("horario", "HN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chave").value("1.0106.19.00-HN"))
                .andExpect(jsonPath("$.descricaoHorario").value("Hora normal"));
    }

    @Test
    void gerar_dadoInvalidoViraBadRequestComMensagem() throws Exception {
        when(service.gerar("9.9", "HN")).thenThrow(new IllegalArgumentException("Código NBS desconhecido: 9.9"));

        mvc.perform(get("/api/smart-key").param("nbs", "9.9").param("horario", "HN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Código NBS desconhecido: 9.9"));
    }

    @Test
    void gerar_semParametrosViraBadRequest() throws Exception {
        mvc.perform(get("/api/smart-key")).andExpect(status().isBadRequest());
    }
}
