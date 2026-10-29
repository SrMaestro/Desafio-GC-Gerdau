package com.Gerdau.Desafio_GC_Gerdau_App.controller;

import com.Gerdau.Desafio_GC_Gerdau_App.config.SecurityConfig;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ConsultaCadastro;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SugestaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ConsultaCadastroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConsultaViewController.class)
@Import(SecurityConfig.class)
@WithMockUser(roles = "APROVADOR")
class ConsultaViewControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ConsultaCadastroRepository repository;

    private ConsultaCadastro consulta(long id, String texto, ResultadoConsulta resultado, DecisaoConsulta decisao) {
        ConsultaCadastro c = new ConsultaCadastro();
        c.setId(id);
        c.setCriadoEm(LocalDateTime.of(2026, 10, 28, 14, 30));
        c.setUsuario("ana");
        c.setTextoBreve(texto);
        c.setCondicao("Hora normal");
        c.setCnae("4399103");
        c.setCodigoGerado("SOLD_HN4399103");
        c.setResultado(resultado);
        c.setDecisao(decisao);
        return c;
    }

    @Test
    void lista_mostraAsConsultas() throws Exception {
        when(repository.findTop200ByOrderByCriadoEmDesc()).thenReturn(List.of(
                consulta(1, "Solda de tubo", ResultadoConsulta.BLOQUEADO, null),
                consulta(2, "Pintura", ResultadoConsulta.CADASTRADO, DecisaoConsulta.CONFIRMOU_MESMO_ASSIM)));

        mvc.perform(get("/ui/consultas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Solda de tubo")))
                .andExpect(content().string(containsString("ana")))
                .andExpect(content().string(containsString("28/10/2026 14:30")))
                .andExpect(content().string(containsString("Bloqueado")))
                .andExpect(content().string(containsString("Cadastrou mesmo assim")))
                .andExpect(content().string(containsString("href=\"/ui/consultas/1\"")));
    }

    @Test
    void lista_vazia_mostraMensagem() throws Exception {
        when(repository.findTop200ByOrderByCriadoEmDesc()).thenReturn(List.of());

        mvc.perform(get("/ui/consultas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhuma consulta registrada")));
    }

    @Test
    void detalhe_mostraSugestoesEScores() throws Exception {
        ConsultaCadastro c = consulta(3, "Solda de tubo", ResultadoConsulta.SUGERIDO, DecisaoConsulta.REUTILIZOU_EXISTENTE);
        c.setServicoId(7);
        SugestaoConsulta s = new SugestaoConsulta();
        s.setConsulta(c);
        s.setServicoId(7);
        s.setCodigoPadronizado("SOLTUB_HN4399103");
        s.setTextoBreve("Soldagem de tubos");
        s.setScore(0.62);
        c.getSugestoes().add(s);
        when(repository.findComSugestoesById(3L)).thenReturn(Optional.of(c));

        mvc.perform(get("/ui/consultas/3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reutilizou código existente")))
                .andExpect(content().string(containsString("Soldagem de tubos")))
                .andExpect(content().string(containsString("SOLTUB_HN4399103")))
                .andExpect(content().string(containsString("62% parecido")))
                .andExpect(content().string(containsString("href=\"/ui/servicos/7\"")));
    }

    @Test
    void detalhe_idInexistente_da404() throws Exception {
        when(repository.findComSugestoesById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/ui/consultas/99")).andExpect(status().isNotFound());
    }

    @Test
    void textoComHtml_apareceEscapado() throws Exception {
        String texto = "<b>negrito</b> & \"aspas\"";
        ConsultaCadastro c = consulta(4, texto, ResultadoConsulta.BLOQUEADO, null);
        when(repository.findTop200ByOrderByCriadoEmDesc()).thenReturn(List.of(c));
        when(repository.findComSugestoesById(4L)).thenReturn(Optional.of(c));

        mvc.perform(get("/ui/consultas"))
                .andExpect(content().string(containsString("&lt;b&gt;negrito&lt;/b&gt; &amp;")))
                .andExpect(content().string(not(containsString("<b>negrito</b>"))));
        mvc.perform(get("/ui/consultas/4"))
                .andExpect(content().string(containsString("&lt;b&gt;negrito&lt;/b&gt; &amp;")))
                .andExpect(content().string(not(containsString("<b>negrito</b>"))));
    }

    @Test
    void menu_aprovadorVeOLinkDeConsultas() throws Exception {
        when(repository.findTop200ByOrderByCriadoEmDesc()).thenReturn(List.of());

        mvc.perform(get("/ui/consultas"))
                .andExpect(content().string(containsString("href=\"/ui/consultas\"")));
    }

    @Test
    void detalhe_mostraAJustificativaDaContestacao() throws Exception {
        ConsultaCadastro c = consulta(5, "Bomba 20 cv", ResultadoConsulta.CADASTRADO, DecisaoConsulta.CONTESTOU_BLOQUEIO);
        c.setJustificativa("Capacidade diferente <da> de 10 cv");
        when(repository.findComSugestoesById(5L)).thenReturn(Optional.of(c));

        mvc.perform(get("/ui/consultas/5"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Contestou o bloqueio")))
                .andExpect(content().string(containsString("Capacidade diferente &lt;da&gt; de 10 cv")));
    }
}
