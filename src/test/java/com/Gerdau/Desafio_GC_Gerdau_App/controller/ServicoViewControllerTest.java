package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.Indicadores;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
        when(resumoService.indicadores()).thenReturn(new Indicadores(10L, 3L, 2L, 4L));
        when(repository.pesquisar(anyString(), anyString(), any(Pageable.class))).thenReturn(Page.empty());
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
    void menu_apareceUmaUnicaVez() throws Exception {
        String html = mvc.perform(get("/")).andReturn().getResponse().getContentAsString();

        assertThat(html.split("class=\"menu\"", -1)).hasSize(2);
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

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void criar_passaOUsuarioLogadoAoServico() throws Exception {
        com.Gerdau.Desafio_GC_Gerdau_App.model.Servico criado = new com.Gerdau.Desafio_GC_Gerdau_App.model.Servico();
        criado.setId(5);
        criado.setCodigoPadronizado("CONC_HN4399103");
        when(service.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq(false),
                org.mockito.ArgumentMatchers.eq("ana"))).thenReturn(criado);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("areaServico", "Civil").param("textoBreve", "Concretagem").param("abreviatura", "CONC")
                        .param("condicao", "HN").param("cnae", "4399103").param("criadorCodigo", "outra pessoa"))
                .andExpect(status().is3xxRedirection());

        org.mockito.Mockito.verify(service).cadastrar(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(false), org.mockito.ArgumentMatchers.eq("ana"));
    }

    private com.Gerdau.Desafio_GC_Gerdau_App.model.Servico servico(int id, String codigo) {
        com.Gerdau.Desafio_GC_Gerdau_App.model.Servico s = new com.Gerdau.Desafio_GC_Gerdau_App.model.Servico();
        s.setId(id);
        s.setCodigoPadronizado(codigo);
        return s;
    }

    @Test
    void reutilizar_registraEVaiParaODetalheComMensagem() throws Exception {
        when(service.registrarReutilizacao("user", "Solda de tubo", 7)).thenReturn(servico(7, "ABC_HN1234567"));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos/reutilizar")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("textoBreve", "Solda de tubo").param("servicoId", "7"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/ui/servicos/7"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attribute("mensagem", containsString("ABC_HN1234567")));
    }

    @Test
    void reutilizar_servicoInexistente_voltaAoFormularioComErro() throws Exception {
        when(service.registrarReutilizacao("user", "Solda", 99)).thenThrow(new java.util.NoSuchElementException());

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos/reutilizar")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("textoBreve", "Solda").param("servicoId", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/ui/servicos/novo"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash()
                        .attribute("erro", "Serviço não encontrado."));
    }

    @Test
    void reutilizar_semCsrf_eRecusado() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos/reutilizar")
                        .param("textoBreve", "Solda").param("servicoId", "7"))
                .andExpect(status().isForbidden());
    }

    @Test
    void formulario_comParecidos_mostraBotaoUsarEsteCodigo() throws Exception {
        when(repository.findAreas()).thenReturn(List.of("Civil"));
        when(service.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException(
                        new com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade("COD", false,
                                List.of(new com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto(
                                        7, "Solda de tubo", "SOLD_HN1234567", 0.6)), false, null)));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("areaServico", "Civil").param("textoBreve", "Solda de tubo").param("abreviatura", "SOLD")
                        .param("condicao", "HN").param("cnae", "1234567"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usar este código")))
                .andExpect(content().string(containsString("formaction=\"/ui/servicos/reutilizar\"")))
                .andExpect(content().string(containsString("formnovalidate")))
                .andExpect(content().string(containsString("name=\"servicoId\"")));
    }

    @Test
    void formulario_comFlashDeErro_mostraAviso() throws Exception {
        when(repository.findAreas()).thenReturn(List.of());

        mvc.perform(get("/ui/servicos/novo").flashAttr("erro", "Serviço não encontrado."))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Serviço não encontrado.")));
    }

    @Test
    void formulario_expoeAUrlDoBotaoUsarEsteCodigoParaAListaAoDigitar() throws Exception {
        when(repository.findAreas()).thenReturn(List.of());

        mvc.perform(get("/ui/servicos/novo"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-url-reutilizar=\"/ui/servicos/reutilizar\"")));
    }

    @Test
    @WithMockUser(username = "ana", roles = "COMPRADOR")
    void indicadores_todosVeemOsNumerosEATaxa() throws Exception {
        when(resumoService.indicadores()).thenReturn(new Indicadores(40L, 12L, 8L, 15L));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("Duplicados barrados")))
                .andExpect(content().string(containsString("Códigos reaproveitados")))
                .andExpect(content().string(containsString("Sem correspondência")))
                .andExpect(content().string(containsString("Duplicidade evitada")))
                .andExpect(content().string(containsString("resumo-valor\">12<")))
                .andExpect(content().string(containsString("resumo-valor\">8<")))
                .andExpect(content().string(containsString("resumo-valor\">15<")))
                .andExpect(content().string(containsString("resumo-valor\">50%<")));
    }

    @Test
    void indicadores_semTentativasMostraTracoNaTaxaEZeroNosOutros() throws Exception {
        when(resumoService.indicadores()).thenReturn(new Indicadores(0L, 0L, 0L, 0L));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("resumo-valor\">—<")));
    }

    @Test
    void indicadores_contagemIndisponivelMostraTracoEAPaginaAbre() throws Exception {
        when(resumoService.indicadores()).thenReturn(new Indicadores(null, null, null, null));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Duplicados barrados")));
    }

    private org.springframework.test.web.servlet.ResultActions postarBloqueado(
            com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade analise) throws Exception {
        when(repository.findAreas()).thenReturn(List.of("Civil"));
        when(service.cadastrar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException(analise));
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .param("areaServico", "Civil").param("textoBreve", "Bomba 20 cv").param("abreviatura", "BOMBA")
                .param("condicao", "HN").param("cnae", "3314801"));
    }

    private static com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade analise(boolean codigoExistente,
                                                                                    String reconhecidoComo) {
        return new com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade("COD", codigoExistente,
                List.of(new com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade.SimilarDto(
                        7, "Bomba 10 cv", "BOMBA_HN3314801", 0.85)), true, reconhecidoComo);
    }

    @Test
    void bloqueioPorTexto_ofereceCampoDeJustificativaEBotaoDeContestar() throws Exception {
        postarBloqueado(analise(false, null))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"justificativaOutroServico\"")))
                .andExpect(content().string(containsString("É outro serviço")));
    }

    @Test
    void bloqueioPorCodigoExistente_naoOfereceContestacao() throws Exception {
        postarBloqueado(analise(true, null))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"justificativaOutroServico\""))));
    }

    @Test
    void bloqueioPorSinonimo_naoOfereceContestacao() throws Exception {
        postarBloqueado(analise(false, "BOMBA_HN3314801"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("name=\"justificativaOutroServico\""))));
    }

    @Test
    void criar_passaAJustificativaAoServico() throws Exception {
        com.Gerdau.Desafio_GC_Gerdau_App.model.Servico criado = new com.Gerdau.Desafio_GC_Gerdau_App.model.Servico();
        criado.setId(5);
        criado.setCodigoPadronizado("BOMBA_HN3314801");
        org.mockito.ArgumentCaptor<com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm> captor =
                org.mockito.ArgumentCaptor.forClass(com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm.class);
        when(service.cadastrar(captor.capture(), org.mockito.ArgumentMatchers.anyBoolean(),
                org.mockito.ArgumentMatchers.anyString())).thenReturn(criado);

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/ui/servicos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .param("areaServico", "Civil").param("textoBreve", "Bomba 20 cv").param("abreviatura", "BOMBA")
                        .param("condicao", "HN").param("cnae", "3314801")
                        .param("justificativaOutroServico", "Capacidade diferente"))
                .andExpect(status().is3xxRedirection());

        org.assertj.core.api.Assertions.assertThat(captor.getValue().getJustificativaOutroServico())
                .isEqualTo("Capacidade diferente");
    }

    // --- lista paginada ---

    private com.Gerdau.Desafio_GC_Gerdau_App.model.Servico servico(int id) {
        com.Gerdau.Desafio_GC_Gerdau_App.model.Servico s = new com.Gerdau.Desafio_GC_Gerdau_App.model.Servico();
        s.setId(id);
        s.setAreaServico("Civil");
        s.setTextoBreve("Serviço " + id);
        s.setCodigoPadronizado("COD_" + id);
        s.setId2Cnae("4399103");
        return s;
    }

    private Page<com.Gerdau.Desafio_GC_Gerdau_App.model.Servico> paginaDeServicos(int primeiroId, int quantos, int pagina,
                                                                                    long total) {
        return new PageImpl<>(java.util.stream.IntStream.range(primeiroId, primeiroId + quantos)
                .mapToObj(this::servico).toList(), PageRequest.of(pagina, 25), total);
    }

    @Test
    void lista_semParametrosPedeAPrimeiraPaginaDe25() throws Exception {
        when(repository.pesquisar("", "", PageRequest.of(0, 25))).thenReturn(paginaDeServicos(1, 25, 0, 60));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Serviço 25<")))
                .andExpect(content().string(not(containsString("Serviço 26<"))))
                .andExpect(content().string(containsString("1 a 25 de 60")));
    }

    @Test
    void lista_paginaDoMeioTemAnteriorEProximaQueMantemOFiltro() throws Exception {
        when(repository.pesquisar("bomba", "Civil", PageRequest.of(1, 25))).thenReturn(paginaDeServicos(26, 25, 1, 80));

        String html = mvc.perform(get("/").param("q", "bomba").param("area", "Civil").param("pagina", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("26 a 50 de 80")))
                .andExpect(content().string(containsString("Página 2 de 4")))
                .andReturn().getResponse().getContentAsString();

        assertThat(html).contains("rel=\"prev\"").contains("rel=\"next\"");
        assertThat(html).contains("pagina=1").contains("pagina=3");
        assertThat(html).contains("q=bomba").contains("area=Civil");
    }

    @Test
    void lista_primeiraPaginaNaoOferecePaginaAnterior() throws Exception {
        when(repository.pesquisar("", "", PageRequest.of(0, 25))).thenReturn(paginaDeServicos(1, 25, 0, 60));

        mvc.perform(get("/"))
                .andExpect(content().string(not(containsString("rel=\"prev\""))))
                .andExpect(content().string(containsString("rel=\"next\"")));
    }

    @Test
    void lista_umaPaginaSoNaoMostraPaginacao() throws Exception {
        when(repository.pesquisar("", "", PageRequest.of(0, 25))).thenReturn(paginaDeServicos(1, 3, 0, 3));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString("3 serviços")))
                .andExpect(content().string(not(containsString("rel=\"next\""))))
                .andExpect(content().string(not(containsString("Página 1 de"))));
    }

    @Test
    void lista_paginaAlemDoFimMostraAUltima() throws Exception {
        when(repository.pesquisar("", "", PageRequest.of(99, 25))).thenReturn(
                new PageImpl<>(List.of(), PageRequest.of(99, 25), 60));
        when(repository.pesquisar("", "", PageRequest.of(2, 25))).thenReturn(paginaDeServicos(51, 10, 2, 60));

        mvc.perform(get("/").param("pagina", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Serviço 60<")))
                .andExpect(content().string(containsString("Página 3 de 3")));
    }

    @Test
    void lista_paginaInvalidaOuZeroCaiNaPrimeira() throws Exception {
        when(repository.pesquisar("", "", PageRequest.of(0, 25))).thenReturn(paginaDeServicos(1, 2, 0, 2));

        mvc.perform(get("/").param("pagina", "0")).andExpect(status().isOk());
        mvc.perform(get("/").param("pagina", "-4")).andExpect(status().isOk());
        mvc.perform(get("/").param("pagina", "abc")).andExpect(status().isOk());

        verify(repository, org.mockito.Mockito.times(3)).pesquisar("", "", PageRequest.of(0, 25));
    }

    @Test
    void lista_buscaSemResultadoDizQueNadaCorresponde() throws Exception {
        mvc.perform(get("/").param("q", "xyz"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum serviço corresponde à busca")))
                .andExpect(content().string(containsString("value=\"xyz\"")));
    }

    @Test
    void lista_termoEhNormalizadoParaABusca() throws Exception {
        mvc.perform(get("/").param("q", "  Manutenção  ")).andExpect(status().isOk());

        verify(repository).pesquisar(eq("manutencao"), eq(""), eq(PageRequest.of(0, 25)));
    }

    @Test
    void lista_areasVemDoBancoSemCarregarTudo() throws Exception {
        when(repository.findAreas()).thenReturn(List.of("Civil", "Elétrica"));

        mvc.perform(get("/"))
                .andExpect(content().string(containsString(">Civil<")))
                .andExpect(content().string(containsString(">Elétrica<")));
        verify(repository, org.mockito.Mockito.never()).findAll();
    }
}
