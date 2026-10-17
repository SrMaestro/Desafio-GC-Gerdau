package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.DeParaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevisaoViewController.class)
class RevisaoViewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    DeParaService service;

    @MockitoBean
    CatNbsRepository catNbsRepository;

    private DicionarioDePara mapeamento(long id, String nome, String nbs, StatusDePara status) {
        DicionarioDePara m = new DicionarioDePara();
        m.setId(id);
        m.setNomeNormalizado(nome);
        m.setNomeOriginal(nome);
        m.setCodigoNbs(nbs);
        m.setStatus(status);
        m.setConfirmacoes(2);
        return m;
    }

    private CatNbs nbs(String codigo, String descricao) {
        CatNbs c = new CatNbs();
        c.setCodigoNbs(codigo);
        c.setDescricaoAtividade(descricao);
        return c;
    }

    @Test
    void revisao_mostraPendentesComDescricaoEMarcaConflito() throws Exception {
        when(service.listarFila()).thenReturn(List.of(
                mapeamento(1, "manut motor", "1.01", StatusDePara.PENDENTE),
                mapeamento(2, "manut motor", "1.02", StatusDePara.PENDENTE),
                mapeamento(3, "solda tig", "1.03", StatusDePara.PENDENTE)));
        when(service.listarAprovados()).thenReturn(List.of());
        when(catNbsRepository.findAllById(any())).thenReturn(List.of(
                nbs("1.01", "Manutenção de motores"), nbs("1.02", "Instalação elétrica"), nbs("1.03", "Soldagem")));

        mvc.perform(get("/ui/revisao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Manutenção de motores")))
                .andExpect(content().string(containsString("Soldagem")))
                .andExpect(content().string(containsString("selo-conflito")))
                .andExpect(content().string(containsString("Pendentes")));
    }

    @Test
    void revisao_semNadaMostraMensagemVazia() throws Exception {
        when(service.listarFila()).thenReturn(List.of());
        when(service.listarAprovados()).thenReturn(List.of());
        when(catNbsRepository.findAllById(any())).thenReturn(List.of());

        mvc.perform(get("/ui/revisao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nada para revisar")));
    }

    @Test
    void aprovar_redirecionaComMensagem() throws Exception {
        mvc.perform(post("/ui/revisao/1/aprovar").param("revisor", "ana"))
                .andExpect(redirectedUrl("/ui/revisao"))
                .andExpect(flash().attribute("mensagem", "Mapeamento aprovado."));
        verify(service).aprovar(1L, "ana");
    }

    @Test
    void aprovar_erroDeNegocioViraMensagemDeErro() throws Exception {
        when(service.aprovar(1L, "ana")).thenThrow(new IllegalStateException("Já existe mapeamento aprovado"));

        mvc.perform(post("/ui/revisao/1/aprovar").param("revisor", "ana"))
                .andExpect(redirectedUrl("/ui/revisao"))
                .andExpect(flash().attribute("erro", "Já existe mapeamento aprovado"));
    }

    @Test
    void rejeitar_repassaMotivo() throws Exception {
        mvc.perform(post("/ui/revisao/1/rejeitar").param("revisor", "ana").param("motivo", "NBS errado"))
                .andExpect(redirectedUrl("/ui/revisao"))
                .andExpect(flash().attribute("mensagem", "Mapeamento rejeitado."));
        verify(service).rejeitar(1L, "ana", "NBS errado");
    }

    @Test
    void revogar_repassaMotivo() throws Exception {
        mvc.perform(post("/ui/revisao/1/revogar").param("revisor", "ana").param("motivo", "NBS mudou"))
                .andExpect(flash().attribute("mensagem", "Mapeamento revogado."));
        verify(service).revogar(1L, "ana", "NBS mudou");
    }
}
