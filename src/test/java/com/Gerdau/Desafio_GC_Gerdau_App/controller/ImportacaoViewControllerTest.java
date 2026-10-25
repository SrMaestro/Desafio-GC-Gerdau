package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportacaoViewController.class)
@Import(SecurityConfig.class)
class ImportacaoViewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    LoteImportacaoRepository loteRepository;

    private LoteImportacao lote(long id, String dono) {
        LoteImportacao l = new LoteImportacao();
        l.setId(id);
        l.setEnviadoPor(dono);
        l.setArquivoNome("servicos.xlsx");
        l.setStatus(StatusLote.CONCLUIDO);
        return l;
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void listaDeLotes_soOsDoUsuario() throws Exception {
        // A página é só a casca: a lista vem de GET /api/importacoes (que filtra pelo usuário); nada de planilha no HTML
        mvc.perform(get("/ui/importacoes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-url-lotes=\"/api/importacoes\"")))
                .andExpect(content().string(containsString("Meus lotes")));
        verify(loteRepository, org.mockito.Mockito.never()).findAll();
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void lote_mostraOsQuatroBlocosComContagens() throws Exception {
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote(7, "bia")));

        mvc.perform(get("/ui/importacoes/7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-lote-id=\"7\"")))
                .andExpect(content().string(containsString("id=\"bloco-erro\"")))
                .andExpect(content().string(containsString("id=\"bloco-parecida\"")))
                .andExpect(content().string(containsString("id=\"bloco-nova\"")))
                .andExpect(content().string(containsString("id=\"bloco-existente\"")))
                .andExpect(content().string(containsString("id=\"contagem-erro\"")))
                .andExpect(content().string(containsString("Enviar ao aprovador")));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void lote_parecidaComparaCondicaoECnaeDoServicoParecido() throws Exception {
        // a tela resolve a condição da linha pelo catálogo e compara com a do serviço parecido (GET /servicos/{id})
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote(7, "bia")));

        mvc.perform(get("/ui/importacoes/7"))
                .andExpect(content().string(containsString("data-url-horarios=\"/api/horarios\"")));
        String js = Files.readString(Path.of("src/main/resources/static/js/importacao.js"));
        assertThat(js).contains("id1Condicao").contains("id2Cnae").contains("dataset.urlHorarios");
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void lote_deOutroUsuario_404() throws Exception {
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote(7, "carlos")));

        mvc.perform(get("/ui/importacoes/7")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void lote_inexistente_404() throws Exception {
        when(loteRepository.findById(9L)).thenReturn(Optional.empty());

        mvc.perform(get("/ui/importacoes/9")).andExpect(status().isNotFound());
    }

    @Test
    void semLogin_redirecionaParaLogin() throws Exception {
        mvc.perform(get("/ui/importacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/ui/importacoes/7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void cabecalho_mostraLinkImportar() throws Exception {
        mvc.perform(get("/ui/importacoes"))
                .andExpect(content().string(containsString("href=\"/ui/importacoes\"")))
                .andExpect(content().string(containsString(">Importar<")));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void cabecalho_aprovadorTambemVeLinkImportar() throws Exception {
        mvc.perform(get("/ui/importacoes")).andExpect(content().string(containsString(">Importar<")));
    }

    @Test
    void importacaoJs_naoUsaInnerHtmlComDadosDaPlanilha() throws Exception {
        for (String arquivo : List.of("importacao.js", "aprovacao.js", "comum.js")) {
            String js = Files.readString(Path.of("src/main/resources/static/js/" + arquivo));
            assertThat(js).as(arquivo).doesNotContain("innerHTML").doesNotContain("insertAdjacentHTML")
                    .doesNotContain("outerHTML").doesNotContain("document.write");
        }
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void aprovador_veListaDeLotesEnviados() throws Exception {
        mvc.perform(get("/ui/aprovacao"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-url-api=\"/api/importacoes\"")))
                .andExpect(content().string(containsString("id=\"aprovacao-lotes\"")))
                .andExpect(content().string(containsString("/js/comum.js")))
                .andExpect(content().string(containsString("/js/aprovacao.js")));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void comprador_naoAcessaAprovacao_403() throws Exception {
        mvc.perform(get("/ui/aprovacao")).andExpect(status().isForbidden());
        mvc.perform(get("/ui/aprovacao/7")).andExpect(status().isForbidden());
    }

    @Test
    void aprovacao_semLogin_redirecionaParaLogin() throws Exception {
        mvc.perform(get("/ui/aprovacao"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void linkAprovacoes_soParaAprovador() throws Exception {
        mvc.perform(get("/ui/aprovacao")).andExpect(content().string(containsString(">Aprovações<")));
        mvc.perform(get("/ui/importacoes")).andExpect(content().string(containsString("href=\"/ui/aprovacao\"")));
    }

    @Test
    @WithMockUser(username = "bia", roles = "COMPRADOR")
    void linkAprovacoes_compradorNaoVe() throws Exception {
        mvc.perform(get("/ui/importacoes")).andExpect(content().string(not(containsString("Aprovações"))));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void ecraMostraLinhasEmConflitoComOMotivo() throws Exception {
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote(7, "bia")));

        mvc.perform(get("/ui/aprovacao/7"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-lote-id=\"7\"")))
                .andExpect(content().string(containsString("id=\"bloco-conflito\"")))
                .andExpect(content().string(containsString("motivo")))
                .andExpect(content().string(containsString("id=\"filtro-tipo\"")))
                .andExpect(content().string(containsString("id=\"btn-aprovar-bloco\"")))
                .andExpect(content().string(containsString("id=\"btn-rejeitar-bloco\"")));
    }

    @Test
    @WithMockUser(username = "ana", roles = "APROVADOR")
    void loteDaAprovacao_inexistente_404() throws Exception {
        when(loteRepository.findById(9L)).thenReturn(Optional.empty());

        mvc.perform(get("/ui/aprovacao/9")).andExpect(status().isNotFound());
    }

    @Test
    void importacaoJs_mensagensDeFalhaHonestas() throws Exception {
        String js = Files.readString(Path.of("src/main/resources/static/js/importacao.js"));
        assertThat(js).doesNotContain("já lidas foram mantidas")
                .contains("A ação foi salva, mas não consegui atualizar a lista");
    }
}
