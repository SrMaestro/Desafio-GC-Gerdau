package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoClassificacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClassificadorLinhaTest {

    @Mock ServicoRepository servicoRepository;
    @Mock CatHorarioRepository catHorarioRepository;
    @Mock CatNbsRepository catNbsRepository;
    @Mock SinonimoServicoService sinonimoServicoService;

    ClassificadorLinha classificador;

    @BeforeEach
    void setUp() {
        classificador = new ClassificadorLinha(servicoRepository, catHorarioRepository,
                catNbsRepository, sinonimoServicoService);
        CatHorario hn = new CatHorario();
        hn.setDescricaoHorario("Hora normal");
        hn.setSufixoCodigo("HN");
        when(catHorarioRepository.findByAtivoTrueOrderByIdAsc()).thenReturn(List.of(hn));
        when(servicoRepository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(servicoRepository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(sinonimoServicoService.reconhecer(any(), any(), any())).thenReturn(Optional.empty());
    }

    private LinhaPlanilha linha(String abrev, String complemento, String condicao, String cnae) {
        return new LinhaPlanilha(2, "Obras", "Montagem de andaime", abrev, complemento, condicao, cnae, null, null);
    }

    private LinhaPlanilha linhaPadrao() {
        return linha("ANDAIME", null, "HN", "4399103");
    }

    private ServicoSimilar similar(int id, double score, String condicao, String cnae) {
        ServicoSimilar s = mock(ServicoSimilar.class);
        when(s.getId()).thenReturn(id);
        when(s.getScore()).thenReturn(score);
        when(s.getId1Condicao()).thenReturn(condicao);
        when(s.getId2Cnae()).thenReturn(cnae);
        when(s.getTextoBreve()).thenReturn("Andaime existente");
        when(s.getCodigoPadronizado()).thenReturn("OUTRO_HN4399103");
        return s;
    }

    @Test
    void condicaoInexistente_viraErro() {
        ResultadoClassificacao r = classificador.classificar(linha("ANDAIME", null, "hora extra 50%", "4399103"), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.ERRO);
        assertThat(r.motivo()).contains("hora extra 50%");
    }

    @Test
    void cnaeInvalido_viraErro() {
        assertThat(classificador.classificar(linha("ANDAIME", null, "HN", "43991"), Set.of()).situacao())
                .isEqualTo(SituacaoLinha.ERRO);
    }

    @Test
    void cnaeComLetras_viraErro() {
        assertThat(classificador.classificar(linha("ANDAIME", null, "HN", "43991ab"), Set.of()).situacao())
                .isEqualTo(SituacaoLinha.ERRO);
    }

    @Test
    void cnaeComDecimaisNulos_eAceito() {
        ResultadoClassificacao r = classificador.classificar(linha("ANDAIME", null, "HN", "4399103.0"), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.NOVA);
        assertThat(r.codigoGerado()).isEqualTo("ANDAIME_HN4399103");
    }

    @Test
    void codigoCom41Caracteres_viraErro() {
        CatHorario longo = new CatHorario();
        longo.setDescricaoHorario("Posto mensal longo");
        longo.setSufixoCodigo("MENSAL1234");
        when(catHorarioRepository.findByAtivoTrueOrderByIdAsc()).thenReturn(List.of(longo));
        // 10 + 13 (complemento) + "_" + 10 (sufixo) + 7 (CNAE) = 41
        ResultadoClassificacao r = classificador.classificar(
                linha("ABCDEFGHIJ", "_ABCDEFGHIJKL", "MENSAL1234", "4399103"), Set.of());
        assertThat(r.codigoGerado()).hasSize(41);
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.ERRO);
        assertThat(r.motivo()).contains("40");
    }

    @Test
    void condicaoPorDescricao_eAceita() {
        ResultadoClassificacao r = classificador.classificar(linha("andaime", null, "Hora normal", "4399103"), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.NOVA);
        assertThat(r.codigoGerado()).isEqualTo("ANDAIME_HN4399103");
    }

    @Test
    void codigoIgualAExistente_eExistente() {
        Servico s = new Servico();
        s.setId(7);
        when(servicoRepository.findByCodigoPadronizado("ANDAIME_HN4399103")).thenReturn(Optional.of(s));
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.EXISTENTE);
        assertThat(r.servicoParecidoId()).isEqualTo(7);
    }

    @Test
    void sinonimoAprovado_eExistente() {
        Servico s = new Servico();
        s.setId(9);
        s.setCodigoPadronizado("OUTRO_HN4399103");
        when(sinonimoServicoService.reconhecer("Montagem de andaime", "Hora normal", "4399103"))
                .thenReturn(Optional.of(s));
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.EXISTENTE);
        assertThat(r.servicoParecidoId()).isEqualTo(9);
    }

    @Test
    void codigoJaVistoNoLote_eRepetida() {
        Set<String> vistos = new HashSet<>(Set.of("ANDAIME_HN4399103"));
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), vistos);
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.REPETIDA);
        assertThat(vistos).containsExactly("ANDAIME_HN4399103");
    }

    @Test
    void similaridade085MesmaCondicaoECnae_eParecidaNaoExistente() {
        List<ServicoSimilar> encontrados = List.of(similar(3, 0.85, "Hora normal", "4399103"));
        when(servicoRepository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(encontrados);
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.PARECIDA);
        assertThat(r.servicoParecidoId()).isEqualTo(3);
        assertThat(r.score()).isEqualTo(0.85);
    }

    @Test
    void similaridade045_eParecida() {
        List<ServicoSimilar> encontrados = List.of(
                similar(4, 0.45, "Hora normal", "1111111"), similar(5, 0.6, "Hora normal", "1111111"));
        when(servicoRepository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(encontrados);
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.PARECIDA);
        assertThat(r.servicoParecidoId()).isEqualTo(5);
    }

    @Test
    void similaridade039_eNova() {
        // o banco ja filtra pelo limiar: 0.39 nao volta da consulta
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.NOVA);
    }

    @Test
    void semSimilares_eNova() {
        ResultadoClassificacao r = classificador.classificar(linhaPadrao(), Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.NOVA);
        assertThat(r.codigoGerado()).isEqualTo("ANDAIME_HN4399103");
    }

    @Test
    void camposEmBranco_viraErroSemExcecao() {
        LinhaPlanilha vazia = new LinhaPlanilha(5, null, null, null, null, null, null, null, null);
        ResultadoClassificacao r = classificador.classificar(vazia, Set.of());
        assertThat(r.situacao()).isEqualTo(SituacaoLinha.ERRO);
        assertThat(r.motivo()).contains("area_servico");
    }
}
