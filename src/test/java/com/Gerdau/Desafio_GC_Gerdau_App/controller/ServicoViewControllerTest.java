package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.Resumo;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ResumoService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @MockitoBean
    ResumoService resumoService;

    @BeforeEach
    void resumoPadrao() {
        when(resumoService.resumo(anyBoolean())).thenReturn(new Resumo(0L, 0L, 0L));
    }

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

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void cabecalho_compradorVeNomeESairMasNaoRevisao() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("ana")))
                .andExpect(content().string(containsString("action=\"/logout\"")))
                .andExpect(content().string(containsString("<meta name=\"_csrf\"")))
                .andExpect(content().string(not(containsString("/ui/revisao"))));
    }

    @Test
    void cabecalho_aprovadorVeLinkDeRevisao() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("/ui/revisao")));
    }

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void menu_compradorVeServicosBuscaEImportarMasNaoRevisaoNemAprovacoes() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("href=\"/ui/busca\"")))
                .andExpect(content().string(containsString("href=\"/ui/importacoes\"")))
                .andExpect(content().string(containsString("aria-current=\"page\"")))
                .andExpect(content().string(not(containsString("/ui/revisao"))))
                .andExpect(content().string(not(containsString("/ui/aprovacao"))))
                .andExpect(content().string(not(containsString("Aprovações"))));
    }

    @Test
    void menu_aprovadorVeRevisaoEAprovacoes() throws Exception {
        mvc.perform(get("/"))
                .andExpect(content().string(containsString("href=\"/ui/revisao\"")))
                .andExpect(content().string(containsString("href=\"/ui/aprovacao\"")));
    }

    @Test
    void menu_marcaOItemDaTelaAtual() throws Exception {
        mvc.perform(get("/ui/busca"))
                .andExpect(content().string(containsString("href=\"/ui/busca\" aria-current=\"page\"")));
    }

    @Test
    void cards_aprovadorVeOsTresComNumeros() throws Exception {
        when(resumoService.resumo(true)).thenReturn(new Resumo(1122L, 37L, 5L));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Serviços cadastrados")))
                .andExpect(content().string(containsString("Mapeamentos pendentes")))
                .andExpect(content().string(containsString("Linhas aguardando aprovação")))
                .andExpect(content().string(containsString("resumo-valor\">1122<")))
                .andExpect(content().string(containsString("resumo-valor\">37<")))
                .andExpect(content().string(containsString("resumo-valor\">5<")));
    }

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void cards_compradorNaoVeOCardDeAprovacao() throws Exception {
        when(resumoService.resumo(false)).thenReturn(new Resumo(1122L, 37L, null));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Serviços cadastrados")))
                .andExpect(content().string(not(containsString("Linhas aguardando aprovação"))));
    }

    @Test
    void cards_contagemIndisponivelMostraTracoEAPaginaAbre() throws Exception {
        when(resumoService.resumo(true)).thenReturn(new Resumo(null, 37L, 5L));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("resumo-valor\">—<")));
    }

    @Test
    void cards_zeroApareceComoZero() throws Exception {
        when(resumoService.resumo(true)).thenReturn(new Resumo(0L, 0L, 0L));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("resumo-valor\">0<")))
                .andExpect(content().string(not(containsString("resumo-valor\">—<"))));
    }
}
