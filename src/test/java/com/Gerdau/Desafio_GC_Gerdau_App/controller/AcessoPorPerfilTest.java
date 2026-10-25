package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.service.DeParaService;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({DeParaController.class, RevisaoViewController.class})
@Import(SecurityConfig.class)
class AcessoPorPerfilTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DeParaService service;

    @MockitoBean
    CatNbsRepository catNbsRepository;

    private static final String CONFIRMACAO = "{\"nomeDigitado\":\"Motor\",\"codigoNbs\":\"1.01\"}";

    @Test
    void semLogin_apiResponde401SemRedirecionar() throws Exception {
        mvc.perform(get("/api/de-para/fila"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void semLogin_telaRedirecionaParaLogin() throws Exception {
        mvc.perform(get("/ui/revisao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void comprador_naoAcessaFilaDoDePara() throws Exception {
        mvc.perform(get("/api/de-para/fila")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void comprador_podeConfirmar() throws Exception {
        when(service.confirmar("Motor", "1.01", null)).thenReturn(true);

        mvc.perform(post("/api/de-para/confirmacoes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIRMACAO))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void comprador_naoAprova() throws Exception {
        mvc.perform(post("/api/de-para/1/aprovar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COMPRADOR")
    void comprador_naoAcessaTelaDeRevisao() throws Exception {
        mvc.perform(get("/ui/revisao")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "APROVADOR")
    void aprovador_acessaFila() throws Exception {
        when(service.listarFila()).thenReturn(List.of());

        mvc.perform(get("/api/de-para/fila")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "APROVADOR")
    void postSemCsrf_ehRecusado() throws Exception {
        mvc.perform(post("/api/de-para/1/aprovar")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
