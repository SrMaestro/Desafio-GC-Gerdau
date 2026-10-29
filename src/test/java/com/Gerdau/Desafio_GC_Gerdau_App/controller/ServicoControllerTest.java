package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import org.springframework.http.MediaType;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "APROVADOR")
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

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void criar_passaOUsuarioLogadoAoServico() throws Exception {
        Servico criado = new Servico();
        criado.setId(5);
        when(service.cadastrar(any(), eq(false), eq("ana"))).thenReturn(criado);

        mvc.perform(post("/servicos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"areaServico\":\"Civil\",\"textoBreve\":\"Concretagem\",\"abreviatura\":\"CONC\","
                                + "\"condicao\":\"HN\",\"cnae\":\"4399103\",\"criadorCodigo\":\"outra pessoa\"}"))
                .andExpect(status().isCreated());

        verify(service).cadastrar(any(), eq(false), eq("ana"));
    }
}
