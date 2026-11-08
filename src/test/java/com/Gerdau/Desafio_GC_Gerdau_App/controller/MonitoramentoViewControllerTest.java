package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.DuplicataPossivel;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.PainelMonitoramento;
import com.Gerdau.Desafio_GC_Gerdau_App.service.MonitoramentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoramentoViewController.class)
@Import(SecurityConfig.class)
class MonitoramentoViewControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean MonitoramentoService service;

    @Test
    @WithMockUser(roles = "APROVADOR")
    void aprovadorVeIndicadoresDuplicatasEAlertas() throws Exception {
        when(service.painel()).thenReturn(new PainelMonitoramento(1,
                List.of(new DuplicataPossivel(1, "COD_A", "Texto A", 2, "COD_B", "Texto B", 0.93, true)),
                7, 12, 3, 85, 5.5, null, List.of("Fila de aprovação acima do limite (12 > 10)")));
        mvc.perform(get("/ui/monitoramento"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("COD_B")))
                .andExpect(content().string(containsString("93%")))
                .andExpect(content().string(containsString("Idêntica (legado)")))
                .andExpect(content().string(containsString("85%")))
                .andExpect(content().string(containsString("Fila de aprovação acima do limite")));
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void compradorNaoAcessa() throws Exception {
        mvc.perform(get("/ui/monitoramento")).andExpect(status().isForbidden());
    }
}
