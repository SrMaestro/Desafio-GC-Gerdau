package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.AnaliseSimilaridade;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.NovoServicoForm;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.CadastroRejeitadoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    ServicoRepository repository;

    @Mock
    CatNbsRepository catNbsRepository;

    @Mock
    CatHorarioRepository catHorarioRepository;

    @Mock
    SinonimoServicoService sinonimoServicoService;

    @Mock
    RegistroConsultaService registro;

    ServicoService service;

    @BeforeEach
    void setUp() {
        service = new ServicoService(repository, catNbsRepository, catHorarioRepository,
                sinonimoServicoService, registro);
    }

    private void condicaoAtiva(String sufixo, String descricao) {
        CatHorario h = new CatHorario();
        h.setSufixoCodigo(sufixo);
        h.setDescricaoHorario(descricao);
        when(catHorarioRepository.findBySufixoCodigoAndAtivoTrue(sufixo)).thenReturn(Optional.of(h));
    }

    private NovoServicoForm form() {
        NovoServicoForm f = new NovoServicoForm();
        f.setAreaServico("Civil");
        f.setTextoBreve("Concretagem de fundação");
        f.setAbreviatura("conc");
        f.setComplemento("_CONCRE");
        f.setCondicao("HN");
        f.setCnae("4399103");
        return f;
    }

    private ServicoSimilar similar(double score, String condicao, String cnae) {
        return new ServicoSimilar() {
            public Integer getId() { return 1; }
            public String getTextoBreve() { return "Concretagem de fundação"; }
            public String getCodigoPadronizado() { return "CONC_CONCRE_HN4399103"; }
            public String getId1Condicao() { return condicao; }
            public String getId2Cnae() { return cnae; }
            public Double getScore() { return score; }
        };
    }

    @Test
    void analisar_sinonimoAprovado_bloqueiaEInformaOCodigoExistente() {
        condicaoAtiva("HN", "Hora normal");
        Servico existente = new Servico();
        existente.setId(1);
        existente.setTextoBreve("Concretagem de fundação");
        existente.setCodigoPadronizado("CONC_CONCRE_HN4399103");
        when(sinonimoServicoService.reconhecer("Concretagem de fundação", "Hora normal", "4399103"))
                .thenReturn(Optional.of(existente));

        AnaliseSimilaridade analise = service.analisar(form());

        assertTrue(analise.bloqueado());
        assertEquals("CONC_CONCRE_HN4399103", analise.reconhecidoComo());
        assertEquals("CONC_CONCRE_HN4399103", analise.similares().get(0).codigoPadronizado());
        assertEquals(1.0, analise.similares().get(0).score());
    }

    @Test
    void cadastrar_sinonimoAprovado_lancaCadastroRejeitado() {
        condicaoAtiva("HN", "Hora normal");
        Servico existente = new Servico();
        existente.setId(1);
        existente.setCodigoPadronizado("CONC_CONCRE_HN4399103");
        when(sinonimoServicoService.reconhecer(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(existente));

        CadastroRejeitadoException e = assertThrows(CadastroRejeitadoException.class,
                () -> service.cadastrar(form(), true, "ana"));

        assertEquals("CONC_CONCRE_HN4399103", e.getAnalise().reconhecidoComo());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void geraCodigoNoPadraoDoCatalogo() {
        condicaoAtiva("HN", "Hora normal");
        assertEquals("CONC_CONCRE_HN4399103", service.gerarCodigo(form()));
    }

    @Test
    void similaresDevolveOsServicosParecidosComOScore() {
        when(repository.buscarSimilares("concretagem", ServicoService.LIMIAR_SUGESTAO, 5))
                .thenReturn(List.of(similar(0.7, "Hora normal", "4399103")));

        var similares = service.similares("  concretagem ");

        assertEquals(1, similares.size());
        assertEquals("CONC_CONCRE_HN4399103", similares.get(0).codigoPadronizado());
        assertEquals(0.7, similares.get(0).score());
    }

    @Test
    void similaresIgnoraTextoCurtoOuVazio() {
        assertTrue(service.similares(null).isEmpty());
        assertTrue(service.similares(" ab ").isEmpty());
        verify(repository, never()).buscarSimilares(anyString(), anyDouble(), anyInt());
    }

    @Test
    void bloqueiaQuandoCodigoJaExiste() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado("CONC_CONCRE_HN4399103")).thenReturn(Optional.of(new Servico()));
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        AnaliseSimilaridade analise = service.analisar(form());

        assertTrue(analise.codigoExistente());
        assertTrue(analise.bloqueado());
    }

    @Test
    void bloqueiaTextoQuaseIdenticoComMesmaCondicaoECnae() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.buscarDuplicado("Concretagem de fundação", ServicoService.LIMIAR_BLOQUEIO,
                "Hora normal", "4399103")).thenReturn(Optional.of(similar(0.9, "Hora normal", "4399103")));

        assertTrue(service.analisar(form()).bloqueado());
    }

    private ServicoSimilar similarComId(int id, String codigo, double score, String condicao, String cnae) {
        return new ServicoSimilar() {
            public Integer getId() { return id; }
            public String getTextoBreve() { return "Concretagem de fundação"; }
            public String getCodigoPadronizado() { return codigo; }
            public String getId1Condicao() { return condicao; }
            public String getId2Cnae() { return cnae; }
            public Double getScore() { return score; }
        };
    }

    private List<ServicoSimilar> cincoDeOutrasCondicoes() {
        return List.of(
                similarComId(11, "CONC_CONCRE_HES4399103", 1.0, "Hora extra Seg a Sab", "4399103"),
                similarComId(12, "CONC_CONCRE_HED4399103", 1.0, "Hora extra Dom/Feriado", "4399103"),
                similarComId(13, "CONC_CONCRE_ADN4399103", 1.0, "Adicional noturno (hora)", "4399103"),
                similarComId(14, "CONC_CONCRE_HEN4399103", 1.0, "Hora extra noturna Seg a Sab", "4399103"),
                similarComId(15, "CONC_CONCRE_RET4399103", 1.0, "Retroativo", "4399103"));
    }

    @Test
    void bloqueio_naoDependeDeOServicoDuplicadoEstarEntreAsSugestoes() {
        // as 5 sugestões são todas de outras condições; o duplicado verdadeiro ficou de fora delas
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(cincoDeOutrasCondicoes());
        when(repository.buscarDuplicado(anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(Optional.of(similarComId(1, "CONC_CONCRE_HN4399103", 1.0, "Hora normal", "4399103")));

        AnaliseSimilaridade analise = service.analisar(form());

        assertTrue(analise.bloqueado());
    }

    @Test
    void bloqueado_mostraODuplicadoPrimeiroNasSugestoesMesmoForaDoTop5() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(cincoDeOutrasCondicoes());
        when(repository.buscarDuplicado(anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(Optional.of(similarComId(1, "CONC_CONCRE_HN4399103", 1.0, "Hora normal", "4399103")));

        AnaliseSimilaridade analise = service.analisar(form());

        assertEquals(6, analise.similares().size());
        assertEquals("CONC_CONCRE_HN4399103", analise.similares().get(0).codigoPadronizado());
    }

    @Test
    void bloqueado_comODuplicadoJaNasSugestoes_naoRepete() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        ServicoSimilar duplicado = similarComId(1, "CONC_CONCRE_HN4399103", 0.9, "Hora normal", "4399103");
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of(duplicado));
        when(repository.buscarDuplicado(anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(Optional.of(duplicado));

        AnaliseSimilaridade analise = service.analisar(form());

        assertEquals(1, analise.similares().size());
    }

    @Test
    void semDuplicado_asSugestoesNaoMudam() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(cincoDeOutrasCondicoes());

        AnaliseSimilaridade analise = service.analisar(form());

        assertFalse(analise.bloqueado());
        assertEquals(5, analise.similares().size());
    }

    @Test
    void textoIdenticoComOutraCondicaoSoPedeConfirmacao() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.95, "Hora extra Seg a Sab", "4399103")));

        AnaliseSimilaridade analise = service.analisar(form());

        assertFalse(analise.bloqueado());
        assertTrue(analise.requerConfirmacao());
    }

    @Test
    void cadastrarSemConfirmarComParecidosEhRejeitado() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(form(), false, "ana"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrarComConfirmacaoSalvaComCodigoGerado() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

        Servico salvo = service.cadastrar(form(), true, "ana");

        assertEquals("CONC_CONCRE_HN4399103", salvo.getCodigoPadronizado());
        assertEquals("Hora normal", salvo.getId1Condicao());
        assertEquals("CONC", salvo.getAbreviatura());
    }

    @Test
    void rejeitaCodigoMaiorQue40Caracteres() {
        condicaoAtiva("PERS", "Adicional periculosidade s/ hora extra");
        NovoServicoForm f = form();
        f.setAbreviatura("ABCDEFGHIJ");
        f.setComplemento("_ABCDEFGHIJKLMNOPQRS");
        f.setCondicao("PERS");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false, "ana"));
    }

    @Test
    void cadastrar_guardaOCodigoNbsQuandoExiste() {
        condicaoAtiva("HN", "Hora normal");
        NovoServicoForm f = form();
        f.setCodigoNbs(" 1.0105.40.00 ");
        when(catNbsRepository.existsById("1.0105.40.00")).thenReturn(true);
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

        Servico salvo = service.cadastrar(f, false, "ana");

        assertEquals("1.0105.40.00", salvo.getCodigoNbs());
    }

    @Test
    void cadastrar_semNbsContinuaFuncionando() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

        assertNull(service.cadastrar(form(), false, "ana").getCodigoNbs());
    }

    @Test
    void cadastrar_recusaCodigoNbsInexistenteSemSalvar() {
        NovoServicoForm f = form();
        f.setCodigoNbs("9.9999.99.99");
        when(catNbsRepository.existsById("9.9999.99.99")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false, "ana"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrar_recusaCondicaoInexistenteOuAposentadaSemSalvar() {
        NovoServicoForm f = form();
        f.setCondicao("XX");
        when(catHorarioRepository.findBySufixoCodigoAndAtivoTrue("XX")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false, "ana"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrar_aceitaCondicaoNovaInseridaNoCatalogo() {
        NovoServicoForm f = form();
        f.setCondicao("novo");
        condicaoAtiva("NOVO", "Condição nova");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));

        Servico salvo = service.cadastrar(f, false, "ana");

        assertEquals("CONC_CONCRE_NOVO4399103", salvo.getCodigoPadronizado());
        assertEquals("Condição nova", salvo.getId1Condicao());
    }

    // --- registro das consultas ---

    private void semParecidos() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
    }

    @Test
    void cadastrar_bloqueado_registraBloqueadoEAindaLancaExcecao() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.buscarDuplicado(anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(Optional.of(similar(0.9, "Hora normal", "4399103")));

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(form(), true, "ana"));

        verify(registro).registrar(eq("ana"), eq("Concretagem de fundação"), eq("Hora normal"), eq("4399103"),
                eq("CONC_CONCRE_HN4399103"), eq(ResultadoConsulta.BLOQUEADO), isNull(), isNull(), anyList(), isNull());
    }

    @Test
    void cadastrar_semConfirmarComParecidos_registraRequerConfirmacao() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(form(), false, "ana"));

        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.REQUER_CONFIRMACAO), isNull(), isNull(), anyList(), isNull());
    }

    @Test
    void cadastrar_semParecidos_registraCadastradoCadastrouNovoComIdCriado() {
        semParecidos();
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(42);
            return s;
        });

        service.cadastrar(form(), false, "ana");

        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.CADASTRADO), eq(DecisaoConsulta.CADASTROU_NOVO), eq(42), anyList(), isNull());
    }

    @Test
    void cadastrar_comConfirmacaoEParecidos_registraConfirmouMesmoAssim() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt()))
                .thenReturn(List.of(similar(0.5, "Hora extra Seg a Sab", "4399103")));
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(43);
            return s;
        });

        service.cadastrar(form(), true, "ana");

        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.CADASTRADO), eq(DecisaoConsulta.CONFIRMOU_MESMO_ASSIM), eq(43), anyList(), isNull());
    }

    @Test
    void cadastrar_corridaDeUnicidade_registraBloqueado() {
        semParecidos();
        when(repository.saveAndFlush(any(Servico.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicado"));

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(form(), false, "ana"));

        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.BLOQUEADO), isNull(), isNull(), anyList(), isNull());
    }

    @Test
    void cadastrar_erroDeValidacao_naoRegistra() {
        condicaoAtiva("PERS", "Adicional periculosidade s/ hora extra");
        NovoServicoForm f = form();
        f.setAbreviatura("ABCDEFGHIJ");
        f.setComplemento("_ABCDEFGHIJKLMNOPQRS");
        f.setCondicao("PERS");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false, "ana"));

        verify(registro, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void cadastrar_registroLancaExcecao_cadastroContinua() {
        semParecidos();
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("falha ao gravar")).when(registro)
                .registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());

        Servico salvo = service.cadastrar(form(), false, "ana");

        assertEquals("CONC_CONCRE_HN4399103", salvo.getCodigoPadronizado());
    }

    @Test
    void registrarReutilizacao_gravaSugeridoReutilizouEDevolveOServico() {
        Servico existente = new Servico();
        existente.setId(7);
        existente.setCodigoPadronizado("CONC_CONCRE_HN4399103");
        when(repository.findById(7)).thenReturn(Optional.of(existente));
        when(repository.buscarSimilares("Concretagem", ServicoService.LIMIAR_SUGESTAO, 5))
                .thenReturn(List.of(similar(0.7, "Hora normal", "4399103")));

        Servico devolvido = service.registrarReutilizacao("ana", " Concretagem ", 7);

        assertSame(existente, devolvido);
        verify(registro).registrar(eq("ana"), eq("Concretagem"), isNull(), isNull(), isNull(),
                eq(ResultadoConsulta.SUGERIDO), eq(DecisaoConsulta.REUTILIZOU_EXISTENTE), eq(7), anyList(), isNull());
    }

    @Test
    void registrarReutilizacao_servicoInexistente_lancaNoSuchElementESemRegistro() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.registrarReutilizacao("ana", "x", 99));

        verify(registro, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // --- contestação do bloqueio por texto ---

    private void bloqueadoPorTexto() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        when(repository.buscarDuplicado(anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(Optional.of(similarComId(1, "CONC_CONCRE_HN4399103", 0.85, "Hora normal", "4399103")));
    }

    @Test
    void cadastrar_bloqueadoPorTexto_comJustificativa_cadastraEGravaContestou() {
        bloqueadoPorTexto();
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(50);
            return s;
        });
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("  Capacidade diferente: 20 cv  ");

        Servico salvo = service.cadastrar(f, false, "ana");

        assertEquals(50, salvo.getId());
        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.CADASTRADO), eq(DecisaoConsulta.CONTESTOU_BLOQUEIO), eq(50), anyList(),
                eq("Capacidade diferente: 20 cv"));
    }

    @Test
    void cadastrar_bloqueadoPorTexto_justificativaEmBranco_continuaBloqueado() {
        bloqueadoPorTexto();
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("   ");

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(f, true, "ana"));

        verify(repository, never()).saveAndFlush(any());
        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.BLOQUEADO), isNull(), isNull(), anyList(), isNull());
    }

    @Test
    void cadastrar_bloqueadoPorTexto_justificativaCurta_lancaIllegalArgumentSemSalvarNemRegistrar() {
        bloqueadoPorTexto();
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("outro");

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(f, false, "ana"));

        verify(repository, never()).saveAndFlush(any());
        verify(registro, never()).registrar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void cadastrar_codigoJaExiste_justificativaNaoLibera() {
        condicaoAtiva("HN", "Hora normal");
        when(repository.findByCodigoPadronizado("CONC_CONCRE_HN4399103")).thenReturn(Optional.of(new Servico()));
        when(repository.buscarSimilares(anyString(), anyDouble(), anyInt())).thenReturn(List.of());
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("É outro serviço, com certeza");

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(f, true, "ana"));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrar_sinonimoAprovado_justificativaNaoLibera() {
        condicaoAtiva("HN", "Hora normal");
        Servico existente = new Servico();
        existente.setId(1);
        existente.setCodigoPadronizado("CONC_CONCRE_HN4399103");
        when(sinonimoServicoService.reconhecer(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(existente));
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("É outro serviço, com certeza");

        assertThrows(CadastroRejeitadoException.class, () -> service.cadastrar(f, true, "ana"));

        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void cadastrar_semBloqueio_justificativaEhIgnoradaECadastroNaoViraContestacao() {
        semParecidos();
        when(repository.saveAndFlush(any(Servico.class))).thenAnswer(i -> {
            Servico s = i.getArgument(0);
            s.setId(51);
            return s;
        });
        NovoServicoForm f = form();
        f.setJustificativaOutroServico("Texto sem necessidade");

        service.cadastrar(f, false, "ana");

        verify(registro).registrar(eq("ana"), anyString(), anyString(), anyString(), anyString(),
                eq(ResultadoConsulta.CADASTRADO), eq(DecisaoConsulta.CADASTROU_NOVO), eq(51), anyList(), isNull());
    }
}
