package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.DeParaNaoEncontradoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.service.DeParaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeParaController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "ana", roles = "APROVADOR")
class DeParaControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DeParaService service;

    private DicionarioDePara mapeamento(long id, StatusDePara status) {
        DicionarioDePara m = new DicionarioDePara();
        m.setId(id);
        m.setNomeNormalizado("motor");
        m.setNomeOriginal("Motor");
        m.setCodigoNbs("1.01");
        m.setStatus(status);
        m.setConfirmacoes(3);
        return m;
    }

    @Test
    void confirmar_respondeCreatedQuandoRegistra() throws Exception {
        when(service.confirmar("Motor", "1.01", 0.7f)).thenReturn(true);

        mvc.perform(post("/api/de-para/confirmacoes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeDigitado\":\"Motor\",\"codigoNbs\":\"1.01\",\"score\":0.7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrado").value(true));
    }

    @Test
    void confirmar_respondeOkQuandoIgnorada() throws Exception {
        when(service.confirmar(any(), any(), any())).thenReturn(false);

        mvc.perform(post("/api/de-para/confirmacoes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeDigitado\":\"Motor\",\"codigoNbs\":\"1.01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrado").value(false));
    }

    @Test
    void confirmar_rejeitaCorpoSemNome() throws Exception {
        mvc.perform(post("/api/de-para/confirmacoes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigoNbs\":\"1.01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmar_nbsDesconhecidoViraBadRequest() throws Exception {
        when(service.confirmar(any(), eq("9.99"), any()))
                .thenThrow(new IllegalArgumentException("Código NBS desconhecido: 9.99"));

        mvc.perform(post("/api/de-para/confirmacoes").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeDigitado\":\"Motor\",\"codigoNbs\":\"9.99\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Código NBS desconhecido: 9.99"));
    }

    @Test
    void fila_listaPendentes() throws Exception {
        when(service.listarFila()).thenReturn(List.of(mapeamento(1, StatusDePara.PENDENTE)));

        mvc.perform(get("/api/de-para/fila"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].codigoNbs").value("1.01"))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$[0].confirmacoes").value(3));
    }

    @Test
    void aprovar_devolveMapeamentoAprovado() throws Exception {
        when(service.aprovar(1L, "ana")).thenReturn(mapeamento(1, StatusDePara.APROVADO));

        mvc.perform(post("/api/de-para/1/aprovar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void aprovar_usaUsuarioLogadoComoRevisor() throws Exception {
        when(service.aprovar(1L, "ana")).thenReturn(mapeamento(1, StatusDePara.APROVADO));

        mvc.perform(post("/api/de-para/1/aprovar").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        verify(service).aprovar(1L, "ana");
    }

    @Test
    void aprovar_ignoraRevisorDoCorpo() throws Exception {
        when(service.aprovar(1L, "ana")).thenReturn(mapeamento(1, StatusDePara.APROVADO));

        mvc.perform(post("/api/de-para/1/aprovar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"revisor\":\"outro\"}"))
                .andExpect(status().isOk());
        verify(service).aprovar(1L, "ana");
    }

    @Test
    void aprovar_inexistenteViraNotFound() throws Exception {
        when(service.aprovar(99L, "ana")).thenThrow(new DeParaNaoEncontradoException(99L));

        mvc.perform(post("/api/de-para/99/aprovar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Mapeamento não encontrado: 99"));
    }

    @Test
    void aprovar_transicaoInvalidaViraConflict() throws Exception {
        when(service.aprovar(1L, "ana")).thenThrow(new IllegalStateException("Já existe mapeamento aprovado"));

        mvc.perform(post("/api/de-para/1/aprovar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já existe mapeamento aprovado"));
    }

    @Test
    void rejeitar_repassaRevisorEMotivo() throws Exception {
        when(service.rejeitar(1L, "ana", "NBS errado")).thenReturn(mapeamento(1, StatusDePara.REJEITADO));

        mvc.perform(post("/api/de-para/1/rejeitar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"NBS errado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"));
        verify(service).rejeitar(1L, "ana", "NBS errado");
    }

    @Test
    void revogar_repassaRevisorEMotivo() throws Exception {
        when(service.revogar(1L, "ana", "NBS mudou")).thenReturn(mapeamento(1, StatusDePara.REVOGADO));

        mvc.perform(post("/api/de-para/1/revogar").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"NBS mudou\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOGADO"));
    }
}
