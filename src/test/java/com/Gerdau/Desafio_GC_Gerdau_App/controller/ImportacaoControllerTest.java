package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.FiltroAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoLote;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.ArquivoInvalidoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.service.AprovacaoImportacaoService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ConferenciaService;
import com.Gerdau.Desafio_GC_Gerdau_App.service.ImportacaoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportacaoController.class)
@Import(SecurityConfig.class)
class ImportacaoControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ImportacaoService importacaoService;
    @MockitoBean
    ConferenciaService conferenciaService;
    @MockitoBean
    AprovacaoImportacaoService aprovacaoService;
    @MockitoBean
    LoteImportacaoRepository loteRepository;
    @MockitoBean
    LinhaImportacaoRepository linhaRepository;

    private static MockMultipartFile arquivo() {
        return new MockMultipartFile("arquivo", "servicos.xlsx", "application/octet-stream", new byte[]{1, 2});
    }

    private LoteImportacao lote(long id, String dono) {
        LoteImportacao l = new LoteImportacao();
        l.setId(id);
        l.setEnviadoPor(dono);
        l.setArquivoNome("servicos.xlsx");
        l.setStatus(StatusLote.CONCLUIDO);
        return l;
    }

    private ResumoLote resumo(long id) {
        return new ResumoLote(id, "servicos.xlsx", StatusLote.CONCLUIDO, 10, 10,
                java.util.Map.of(SituacaoLinha.NOVA, 10L), java.util.Map.of(EtapaLinha.PRONTA, 10L));
    }

    @Test
    void listaDeLotes_semLogin_401() throws Exception {
        mvc.perform(get("/api/importacoes")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void listaDeLotes_soOsDoUsuarioLogado() throws Exception {
        LoteImportacao l = lote(4, "bia");
        l.setTotalLinhas(10);
        l.setProcessadas(10);
        when(loteRepository.findTop50ByEnviadoPorOrderByCriadoEmDescIdDesc("bia")).thenReturn(List.of(l));

        mvc.perform(get("/api/importacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].arquivo").value("servicos.xlsx"))
                .andExpect(jsonPath("$[0].status").value("CONCLUIDO"))
                .andExpect(jsonPath("$[0].total").value(10))
                .andExpect(jsonPath("$[0].processadas").value(10));
        verify(loteRepository, never()).findAll();
        verify(loteRepository, never()).findTop50ByEnviadoPorOrderByCriadoEmDescIdDesc("outra");
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void listaDeLotes_aprovadorTambemVeOsProprios() throws Exception {
        when(loteRepository.findTop50ByEnviadoPorOrderByCriadoEmDescIdDesc("ana")).thenReturn(List.of());

        mvc.perform(get("/api/importacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void semLogin_401() throws Exception {
        mvc.perform(get("/api/importacoes/1")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/importacoes/aprovacao/lotes")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void comprador_enviaArquivo_201() throws Exception {
        when(importacaoService.iniciar(eq("servicos.xlsx"), any(), eq("bia"))).thenReturn(7L);

        mvc.perform(multipart("/api/importacoes").file(arquivo()).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void arquivoInvalido_400ComMensagem() throws Exception {
        when(importacaoService.iniciar(any(), any(), any())).thenThrow(new ArquivoInvalidoException("Formato inválido"));

        mvc.perform(multipart("/api/importacoes").file(arquivo()).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Formato inválido"));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void semArquivo_400() throws Exception {
        mvc.perform(multipart("/api/importacoes").with(csrf())).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void comprador_naoChamaAprovacao_403() throws Exception {
        mvc.perform(get("/api/importacoes/aprovacao/lotes")).andExpect(status().isForbidden());
        mvc.perform(post("/api/importacoes/aprovacao/linhas/5/aprovar").with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post("/api/importacoes/aprovacao/3/aprovar-em-bloco").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isForbidden());
        verifyNoInteractions(aprovacaoService);
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_aprovaEmBloco_200() throws Exception {
        when(aprovacaoService.aprovarEmBloco(eq(3L), any(), eq("ana"))).thenReturn(new ResumoAprovacao(8, 1, 2));

        mvc.perform(post("/api/importacoes/aprovacao/3/aprovar-em-bloco").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"situacoes\":[\"NOVA\"],\"scoreMinimo\":0.5,\"aprovador\":\"outro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovadas").value(8))
                .andExpect(jsonPath("$.conflitos").value(1))
                .andExpect(jsonPath("$.falhas").value(2));

        ArgumentCaptor<FiltroAprovacao> filtro = ArgumentCaptor.forClass(FiltroAprovacao.class);
        verify(aprovacaoService).aprovarEmBloco(eq(3L), filtro.capture(), eq("ana"));
        assertThat(filtro.getValue().situacoes()).containsExactly(SituacaoLinha.NOVA);
        assertThat(filtro.getValue().scoreMinimo()).isEqualTo(0.5);
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_aprovaERejeitaLinha() throws Exception {
        when(aprovacaoService.aprovar(5L, "ana")).thenReturn(EtapaLinha.APROVADA);
        mvc.perform(post("/api/importacoes/aprovacao/linhas/5/aprovar").with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.etapa").value("APROVADA"));

        mvc.perform(post("/api/importacoes/aprovacao/linhas/5/rejeitar").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"duplicado\"}"))
                .andExpect(status().isOk());
        verify(aprovacaoService).rejeitar(5L, "ana", "duplicado");

        when(aprovacaoService.rejeitarEmBloco(eq(3L), any(), eq("ana"), eq("ruim"))).thenReturn(4);
        mvc.perform(post("/api/importacoes/aprovacao/3/rejeitar-em-bloco").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"ruim\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rejeitadas").value(4));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_listaLotesComEnviadas() throws Exception {
        LoteImportacao l = lote(3, "bia");
        when(linhaRepository.lotesComLinhasEnviadas()).thenReturn(java.util.Collections.singletonList(
                new Object[]{3L, "servicos.xlsx", "bia", null, 12L}));

        mvc.perform(get("/api/importacoes/aprovacao/lotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].enviadoPor").value("bia"))
                .andExpect(jsonPath("$[0].enviadas").value(12));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void comprador_loteDeOutro_404() throws Exception {
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "carlos")));

        mvc.perform(get("/api/importacoes/3")).andExpect(status().isNotFound());
        mvc.perform(get("/api/importacoes/3/linhas")).andExpect(status().isNotFound());
        verifyNoInteractions(importacaoService);
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void comprador_abreOProprioLote() throws Exception {
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "bia")));
        when(importacaoService.resumo(3L)).thenReturn(resumo(3));

        mvc.perform(get("/api/importacoes/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.porSituacao.NOVA").value(10));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_abreLoteDeOutroComEnviadas_soVeLinhasEnviadas() throws Exception {
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "bia")));
        when(linhaRepository.existsByLoteIdAndEtapa(3L, EtapaLinha.ENVIADA)).thenReturn(true);
        when(linhaRepository.findByLoteIdAndEtapa(eq(3L), eq(EtapaLinha.ENVIADA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mvc.perform(get("/api/importacoes/3/linhas?etapa=EM_CONFERENCIA")).andExpect(status().isOk());
        verify(linhaRepository).findByLoteIdAndEtapa(eq(3L), eq(EtapaLinha.ENVIADA), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_loteDeOutroSemEnviadas_404() throws Exception {
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "bia")));
        when(linhaRepository.existsByLoteIdAndEtapa(3L, EtapaLinha.ENVIADA)).thenReturn(false);

        mvc.perform(get("/api/importacoes/3")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void linhas_paginaComTotalETamanhoLimitado() throws Exception {
        LinhaImportacao li = new LinhaImportacao();
        li.setId(9L);
        li.setNumero(2);
        li.setSituacao(SituacaoLinha.NOVA);
        li.setEtapa(EtapaLinha.PRONTA);
        li.setTextoBreve("Texto");
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "bia")));
        when(linhaRepository.findByLoteIdAndSituacao(eq(3L), eq(SituacaoLinha.NOVA), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(li), PageRequest.of(1, 500), 1201));

        mvc.perform(get("/api/importacoes/3/linhas?situacao=NOVA&pagina=1&tamanho=100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1201))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.linhas[0].id").value(9))
                .andExpect(jsonPath("$.linhas[0].numero").value(2))
                .andExpect(jsonPath("$.linhas[0].etapa").value("PRONTA"));

        ArgumentCaptor<Pageable> p = ArgumentCaptor.forClass(Pageable.class);
        verify(linhaRepository).findByLoteIdAndSituacao(eq(3L), eq(SituacaoLinha.NOVA), p.capture());
        assertThat(p.getValue().getPageSize()).isEqualTo(500);
        assertThat(p.getValue().getPageNumber()).isEqualTo(1);
        assertThat(p.getValue().getSort().getOrderFor("numero")).isNotNull();
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void linhas_tamanhoPadraoE100() throws Exception {
        when(loteRepository.findById(3L)).thenReturn(Optional.of(lote(3, "bia")));
        when(linhaRepository.findByLoteId(eq(3L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mvc.perform(get("/api/importacoes/3/linhas")).andExpect(status().isOk());
        ArgumentCaptor<Pageable> p = ArgumentCaptor.forClass(Pageable.class);
        verify(linhaRepository).findByLoteId(eq(3L), p.capture());
        assertThat(p.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void conferencia_usaOPrincipalComoDono_nuncaOCorpo() throws Exception {
        when(conferenciaService.corrigir(eq(9L), any(), eq("bia"))).thenReturn(new LinhaImportacao());
        mvc.perform(put("/api/importacoes/linhas/9").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"textoBreve\":\"Novo\",\"usuario\":\"outro\"}"))
                .andExpect(status().isOk());
        verify(conferenciaService).corrigir(eq(9L), any(), eq("bia"));

        mvc.perform(post("/api/importacoes/linhas/9/decisao").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"decisao\":\"MESMO_SERVICO\"}")).andExpect(status().isOk());
        verify(conferenciaService).decidir(9L, DecisaoParecida.MESMO_SERVICO, "bia");

        when(conferenciaService.decidirEmBloco(3L, 0.8, DecisaoParecida.OUTRO_SERVICO, "bia")).thenReturn(5);
        mvc.perform(post("/api/importacoes/3/decisao-em-bloco").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scoreMinimo\":0.8,\"decisao\":\"OUTRO_SERVICO\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.decididas").value(5));

        mvc.perform(post("/api/importacoes/linhas/9/descartar").with(csrf())).andExpect(status().isOk());
        verify(conferenciaService).descartar(9L, "bia");

        when(conferenciaService.enviarAoAprovador(3L, "bia")).thenReturn(6);
        mvc.perform(post("/api/importacoes/3/enviar").with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.enviadas").value(6));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_tambemConfere() throws Exception {
        mvc.perform(post("/api/importacoes/linhas/9/descartar").with(csrf())).andExpect(status().isOk());
        verify(conferenciaService).descartar(9L, "ana");
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void accessDeniedDoServico_403() throws Exception {
        org.mockito.Mockito.doThrow(new AccessDeniedException("O lote pertence a outro usuário"))
                .when(conferenciaService).descartar(9L, "bia");

        mvc.perform(post("/api/importacoes/linhas/9/descartar").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.erro").value("O lote pertence a outro usuário"));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void erroDeNegocio_mapeadoParaStatus() throws Exception {
        org.mockito.Mockito.doThrow(new java.util.NoSuchElementException("Linha não encontrada"))
                .when(conferenciaService).descartar(1L, "bia");
        org.mockito.Mockito.doThrow(new IllegalStateException("Linha já enviada"))
                .when(conferenciaService).descartar(2L, "bia");
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Informe a decisão"))
                .when(conferenciaService).descartar(3L, "bia");

        mvc.perform(post("/api/importacoes/linhas/1/descartar").with(csrf())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Linha não encontrada"));
        mvc.perform(post("/api/importacoes/linhas/2/descartar").with(csrf())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Linha já enviada"));
        mvc.perform(post("/api/importacoes/linhas/3/descartar").with(csrf())).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Informe a decisão"));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void postSemCsrf_403() throws Exception {
        mvc.perform(multipart("/api/importacoes").file(arquivo())).andExpect(status().isForbidden());
        mvc.perform(post("/api/importacoes/3/enviar")).andExpect(status().isForbidden());
        verifyNoInteractions(importacaoService, conferenciaService);
    }
}
