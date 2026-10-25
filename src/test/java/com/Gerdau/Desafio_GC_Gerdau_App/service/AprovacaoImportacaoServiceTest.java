package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.FiltroAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoAprovacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatHorarioRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.CatNbsRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.ServicoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.SinonimoServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mockito, sem banco: as fronteiras de transação ({@code REQUIRES_NEW} por linha) e o bloqueio pessimista não
 * são exercitados aqui, só a lógica de cada linha e a orquestração. As consultas e os updates guardados são
 * cobertos em {@code LinhaImportacaoRepositoryTest}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AprovacaoImportacaoServiceTest {

    @Mock LinhaImportacaoRepository linhaRepository;
    @Mock ServicoRepository servicoRepository;
    @Mock CatHorarioRepository catHorarioRepository;
    @Mock CatNbsRepository catNbsRepository;
    @Mock SinonimoServicoService sinonimoServicoService;
    @Mock SinonimoServicoRepository sinonimoRepository;

    AprovacaoImportacaoService service;
    LoteImportacao lote;
    Servico existente;

    @BeforeEach
    void setUp() {
        ClassificadorLinha classificador = new ClassificadorLinha(servicoRepository, catHorarioRepository,
                catNbsRepository, sinonimoServicoService);
        AprovadorLinha aprovadorLinha = new AprovadorLinha(linhaRepository, servicoRepository,
                sinonimoServicoService, sinonimoRepository, classificador);
        service = new AprovacaoImportacaoService(aprovadorLinha, linhaRepository);

        lote = new LoteImportacao();
        lote.setId(7L);
        lote.setEnviadoPor("ana");

        CatHorario hn = new CatHorario();
        hn.setDescricaoHorario("Hora normal");
        hn.setSufixoCodigo("HN");
        when(catHorarioRepository.findByAtivoTrueOrderByIdAsc()).thenReturn(List.of(hn));

        existente = new Servico();
        existente.setId(50);
        existente.setCodigoPadronizado("PINTTUB_HN4321000");
        when(servicoRepository.findById(50)).thenReturn(Optional.of(existente));
        when(servicoRepository.findByCodigoPadronizado(anyString())).thenReturn(Optional.empty());
        when(servicoRepository.saveAndFlush(any(Servico.class))).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            s.setId(99);
            return s;
        });
        when(sinonimoServicoService.reconhecer(any(), any(), any())).thenReturn(Optional.empty());
        when(linhaRepository.marcarConflitoSeEnviada(anyLong(), anyString(), anyString(), any())).thenReturn(1);
    }

    private LinhaImportacao linha(long id, SituacaoLinha situacao, DecisaoParecida decisao) {
        LinhaImportacao l = new LinhaImportacao();
        l.setId(id);
        l.setLote(lote);
        l.setNumero((int) id + 1);
        l.setAreaServico(" Manutenção ");
        l.setTextoBreve(" Pintura de tubulação ");
        l.setAbreviatura("pint");
        l.setComplemento("_tub");
        l.setCondicao("hora normal");
        l.setCnae("4321000");
        l.setCriadorCodigo("ana");
        l.setCodigoGerado("PINT_TUB_HN4321000");
        l.setSituacao(situacao);
        l.setEtapa(EtapaLinha.ENVIADA);
        l.setDecisao(decisao);
        if (situacao == SituacaoLinha.PARECIDA) {
            l.setServicoParecidoId(50);
            l.setScore(0.7);
        }
        when(linhaRepository.findByIdParaAprovar(id)).thenReturn(Optional.of(l));
        return l;
    }

    private String motivoConflito(long id) {
        ArgumentCaptor<String> motivo = ArgumentCaptor.forClass(String.class);
        verify(linhaRepository).marcarConflitoSeEnviada(eq(id), motivo.capture(), eq("gil"), any());
        return motivo.getValue();
    }

    // ---- aprovar: linha nova ----

    @Test
    void aprovarNova_criaServico() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCodigoNbs("1.0101");

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(servicoRepository).saveAndFlush(captor.capture());
        Servico s = captor.getValue();
        assertThat(s.getAreaServico()).isEqualTo("Manutenção");
        assertThat(s.getTextoBreve()).isEqualTo("Pintura de tubulação");
        assertThat(s.getAbreviatura()).isEqualTo("PINT");
        assertThat(s.getComplemento()).isEqualTo("_TUB");
        assertThat(s.getId1Condicao()).isEqualTo("Hora normal");
        assertThat(s.getId2Cnae()).isEqualTo("4321000");
        assertThat(s.getCodigoPadronizado()).isEqualTo("PINT_TUB_HN4321000");
        assertThat(s.getCodigoNbs()).isEqualTo("1.0101");
        assertThat(s.getCriadorCodigo()).isEqualTo("ana");
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        assertThat(l.getServicoCriadoId()).isEqualTo(99);
        assertThat(l.getDecididoPor()).isEqualTo("gil");
        assertThat(l.getDecididoEm()).isNotNull();
        verify(linhaRepository).save(l);
        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void aprovarNova_cnaeDoExcel_eCondicaoPeloSufixo() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCnae("4321000.0");
        l.setCondicao("hn");

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
        verify(servicoRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId2Cnae()).isEqualTo("4321000");
        assertThat(captor.getValue().getId1Condicao()).isEqualTo("Hora normal");
    }

    @Test
    void aprovarNova_codigoOcupado_viraConflitoSemGravar() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        when(servicoRepository.findByCodigoPadronizado("PINT_TUB_HN4321000")).thenReturn(Optional.of(existente));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, never()).saveAndFlush(any());
        verify(linhaRepository, never()).save(any());
        assertThat(motivoConflito(1)).contains("PINT_TUB_HN4321000").contains("já foi cadastrado");
        assertThat(l.getServicoCriadoId()).isNull();
    }

    @Test
    void aprovarNova_violacaoDeUnicidade_viraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        // a primeira tentativa perde a corrida no insert; a segunda já vê o código ocupado
        when(servicoRepository.findByCodigoPadronizado("PINT_TUB_HN4321000"))
                .thenReturn(Optional.empty(), Optional.of(existente));
        when(servicoRepository.saveAndFlush(any(Servico.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(linhaRepository, never()).save(any());
        assertThat(motivoConflito(1)).contains("já foi cadastrado");
        assertThat(l.getServicoCriadoId()).isNull();
    }

    @Test
    void aprovarNova_violacaoDeUnicidadeRepetida_viraConflito() {
        linha(1, SituacaoLinha.NOVA, null);
        when(servicoRepository.saveAndFlush(any(Servico.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, times(2)).saveAndFlush(any());
        assertThat(motivoConflito(1)).isNotBlank();
    }

    @Test
    void aprovarNova_condicaoNaoResolvida_viraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCondicao("hora aposentada");

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, never()).saveAndFlush(any());
        assertThat(motivoConflito(1)).contains("Condição").contains("hora aposentada");
    }

    @Test
    void aprovarNova_semCodigoGerado_viraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCodigoGerado(null);

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, never()).saveAndFlush(any());
        assertThat(motivoConflito(1)).isNotBlank();
    }

    @Test
    void aprovarNova_codigoDivergeDoCatalogoAtual_viraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCodigoGerado("PINT_TUB_HE4321000");

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, never()).saveAndFlush(any());
        assertThat(motivoConflito(1)).contains("PINT_TUB_HN4321000");
    }

    @Test
    void aprovarOutroServico_criaServico() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.OUTRO_SERVICO);

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        verify(servicoRepository).saveAndFlush(any(Servico.class));
        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
        assertThat(l.getServicoCriadoId()).isEqualTo(99);
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
    }

    // ---- aprovar: mesmo serviço (sinônimo) ----

    @Test
    void aprovarMesmoServico_registraSinonimo() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        verify(sinonimoServicoService).registrar("Pintura de tubulação", "Hora normal", "4321000", existente,
                "gil", 1L);
        verify(sinonimoRepository).flush();
        verify(servicoRepository, never()).saveAndFlush(any());
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        assertThat(l.getServicoCriadoId()).isNull();
        assertThat(l.getDecididoPor()).isEqualTo("gil");
        assertThat(l.getDecididoEm()).isNotNull();
    }

    @Test
    void aprovarMesmoServico_chaveJaLigadaAoMesmoServico_eIdempotente() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        when(sinonimoServicoService.reconhecer("Pintura de tubulação", "Hora normal", "4321000"))
                .thenReturn(Optional.of(existente));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
    }

    @Test
    void aprovarMesmoServico_chaveLigadaAOutroServico_viraConflito() {
        linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        Servico outro = new Servico();
        outro.setId(60);
        outro.setCodigoPadronizado("OUTRO_HN4321000");
        when(sinonimoServicoService.reconhecer("Pintura de tubulação", "Hora normal", "4321000"))
                .thenReturn(Optional.of(outro));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
        verify(linhaRepository, never()).save(any());
        assertThat(motivoConflito(1)).contains("OUTRO_HN4321000");
    }

    @Test
    void aprovarMesmoServico_servicoNaoExisteMais_viraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        l.setServicoParecidoId(51);
        when(servicoRepository.findById(51)).thenReturn(Optional.empty());

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
        assertThat(motivoConflito(1)).contains("51");
    }

    @Test
    void aprovarMesmoServico_corridaNoSinonimoParaOMesmoServico_aprovaNaSegundaTentativa() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        when(sinonimoServicoService.reconhecer("Pintura de tubulação", "Hora normal", "4321000"))
                .thenReturn(Optional.empty(), Optional.of(existente));
        when(sinonimoServicoService.registrar(any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.APROVADA);

        verify(linhaRepository, never()).marcarConflitoSeEnviada(anyLong(), any(), any(), any());
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
    }

    // ---- defesas ----

    @Test
    void aprovar_linhaNaoEnviada_recusa() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setEtapa(EtapaLinha.PRONTA);

        assertThatThrownBy(() -> service.aprovar(1L, "gil"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está aguardando aprovação");

        verify(servicoRepository, never()).saveAndFlush(any());
        verify(linhaRepository, never()).save(any());
        verify(linhaRepository, never()).marcarConflitoSeEnviada(anyLong(), any(), any(), any());
    }

    @Test
    void aprovar_linhaInexistente_lanca() {
        when(linhaRepository.findByIdParaAprovar(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aprovar(404L, "gil"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void aprovar_erroOuParecidaSemDecisaoEnviadas_viramConflito() {
        LinhaImportacao erro = linha(1, SituacaoLinha.ERRO, null);
        LinhaImportacao semDecisao = linha(2, SituacaoLinha.PARECIDA, null);

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);
        assertThat(service.aprovar(2L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        verify(servicoRepository, never()).saveAndFlush(any());
        verify(sinonimoServicoService, never()).registrar(any(), any(), any(), any(), any(), any());
        assertThat(motivoConflito(1)).isNotBlank();
        assertThat(motivoConflito(2)).isNotBlank();
        assertThat(erro.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(semDecisao.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
    }

    @Test
    void conflito_motivoGravadoECortadoNoTamanhoDaColuna() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCondicao("x".repeat(600));

        assertThat(service.aprovar(1L, "gil")).isEqualTo(EtapaLinha.CONFLITO);

        assertThat(motivoConflito(1)).hasSize(PreparadorLinha.MAX_MOTIVO);
    }

    @Test
    void conflito_linhaJaDecididaPorOutroAprovador_recusa() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        l.setCondicao("inexistente");
        when(linhaRepository.marcarConflitoSeEnviada(anyLong(), anyString(), anyString(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.aprovar(1L, "gil")).isInstanceOf(IllegalStateException.class);
    }

    // ---- rejeitar ----

    @Test
    void rejeitar_exigeMotivo() {
        assertThatThrownBy(() -> service.rejeitar(1L, "gil", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motivo");
        assertThatThrownBy(() -> service.rejeitar(1L, "gil", null))
                .isInstanceOf(IllegalArgumentException.class);
        verify(linhaRepository, never()).rejeitarSeEnviada(anyLong(), any(), any(), any());
    }

    @Test
    void rejeitar_registraMotivoEDecisao() {
        when(linhaRepository.rejeitarSeEnviada(eq(1L), eq("Texto genérico demais"), eq("gil"), any())).thenReturn(1);

        service.rejeitar(1L, "gil", "  Texto genérico demais ");

        verify(linhaRepository).rejeitarSeEnviada(eq(1L), eq("Texto genérico demais"), eq("gil"), any());
    }

    @Test
    void rejeitar_linhaNaoEnviada_recusa() {
        when(linhaRepository.rejeitarSeEnviada(anyLong(), any(), any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.rejeitar(1L, "gil", "duplicado"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejeitar_motivoLongoECortado() {
        when(linhaRepository.rejeitarSeEnviada(anyLong(), any(), any(), any())).thenReturn(1);

        service.rejeitar(1L, "gil", "m".repeat(800));

        ArgumentCaptor<String> motivo = ArgumentCaptor.forClass(String.class);
        verify(linhaRepository).rejeitarSeEnviada(eq(1L), motivo.capture(), eq("gil"), any());
        assertThat(motivo.getValue()).hasSize(PreparadorLinha.MAX_MOTIVO);
    }

    // ---- em bloco ----

    @Test
    void emBloco_soLinhasEnviadas() {
        linha(1, SituacaoLinha.NOVA, null);
        LinhaImportacao l2 = linha(2, SituacaoLinha.NOVA, null);
        l2.setCodigoGerado("PINT_TUB_HN4321000");
        when(linhaRepository.idsEnviadasDoLote(eq(7L), any(), isNull())).thenReturn(List.of(1L, 2L));
        // a 2 saiu de ENVIADA (outro aprovador) entre a seleção e o processamento
        l2.setEtapa(EtapaLinha.APROVADA);

        ResumoAprovacao r = service.aprovarEmBloco(7L, new FiltroAprovacao(Set.of(SituacaoLinha.NOVA), null), "gil");

        assertThat(r).isEqualTo(new ResumoAprovacao(1, 0, 0));
        verify(servicoRepository, times(1)).saveAndFlush(any());
        verify(linhaRepository).idsEnviadasDoLote(7L, Set.of(SituacaoLinha.NOVA), null);
    }

    @Test
    void emBloco_filtroVazio_consultaTodasAsSituacoesMasSoEnviadas() {
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of());

        assertThat(service.aprovarEmBloco(7L, null, "gil")).isEqualTo(new ResumoAprovacao(0, 0, 0));
        assertThat(service.aprovarEmBloco(7L, new FiltroAprovacao(Set.of(), null), "gil"))
                .isEqualTo(new ResumoAprovacao(0, 0, 0));

        // a consulta sempre exige etapa ENVIADA; filtro vazio só alarga as situações
        verify(linhaRepository, times(2)).idsEnviadasDoLote(7L, EnumSet.allOf(SituacaoLinha.class), null);
    }

    @Test
    void emBloco_comScoreMinimoFiltraParecidas() {
        LinhaImportacao l = linha(3, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        l.setScore(0.9);
        FiltroAprovacao filtro = new FiltroAprovacao(Set.of(SituacaoLinha.PARECIDA), 0.85);
        when(linhaRepository.idsEnviadasDoLote(7L, Set.of(SituacaoLinha.PARECIDA), 0.85)).thenReturn(List.of(3L));

        assertThat(service.aprovarEmBloco(7L, filtro, "gil")).isEqualTo(new ResumoAprovacao(1, 0, 0));

        verify(linhaRepository).idsEnviadasDoLote(7L, Set.of(SituacaoLinha.PARECIDA), 0.85);
        verify(sinonimoServicoService).registrar(any(), any(), any(), eq(existente), eq("gil"), eq(3L));
    }

    @Test
    void emBloco_umConflitoNaoDesfazOutras() {
        LinhaImportacao a = linha(1, SituacaoLinha.NOVA, null);
        a.setCodigoGerado("A_HN4321000");
        a.setAbreviatura("A");
        a.setComplemento(null);
        LinhaImportacao b = linha(2, SituacaoLinha.NOVA, null);
        b.setCodigoGerado("B_HN4321000");
        b.setAbreviatura("B");
        b.setComplemento(null);
        LinhaImportacao c = linha(3, SituacaoLinha.NOVA, null);
        c.setCodigoGerado("C_HN4321000");
        c.setAbreviatura("C");
        c.setComplemento(null);
        when(servicoRepository.findByCodigoPadronizado("B_HN4321000")).thenReturn(Optional.of(existente));
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of(1L, 2L, 3L));

        ResumoAprovacao r = service.aprovarEmBloco(7L, new FiltroAprovacao(null, null), "gil");

        assertThat(r).isEqualTo(new ResumoAprovacao(2, 1, 0));
        assertThat(a.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        assertThat(c.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        assertThat(motivoConflito(2)).contains("B_HN4321000");
        verify(servicoRepository, times(2)).saveAndFlush(any());
    }

    // ---- erro técnico x conflito de negócio ----

    @Test
    void erroTecnico_deixaALinhaEnviadaENaoViraConflito() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, null);
        when(servicoRepository.findByCodigoPadronizado(anyString()))
                .thenThrow(new CannotAcquireLockException("lock timeout"));
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of(1L));

        ResumoAprovacao r = service.aprovarEmBloco(7L, null, "gil");

        assertThat(r).isEqualTo(new ResumoAprovacao(0, 0, 1));
        verify(linhaRepository, never()).marcarConflitoSeEnviada(anyLong(), any(), any(), any());
        verify(servicoRepository, never()).saveAndFlush(any());
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
    }

    @Test
    void erroInesperadoNoMeioDoLote_naoParaAsOutrasLinhas() {
        LinhaImportacao a = linha(1, SituacaoLinha.NOVA, null);
        LinhaImportacao b = linha(2, SituacaoLinha.PARECIDA, DecisaoParecida.MESMO_SERVICO);
        LinhaImportacao c = linha(3, SituacaoLinha.NOVA, null);
        c.setAbreviatura("C");
        c.setComplemento(null);
        c.setCodigoGerado("C_HN4321000");
        when(sinonimoServicoService.reconhecer(any(), any(), any())).thenThrow(new NullPointerException("bug"));
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of(1L, 2L, 3L));

        ResumoAprovacao r = service.aprovarEmBloco(7L, null, "gil");

        assertThat(r).isEqualTo(new ResumoAprovacao(2, 0, 1));
        assertThat(a.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        assertThat(b.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(c.getEtapa()).isEqualTo(EtapaLinha.APROVADA);
        verify(linhaRepository, never()).marcarConflitoSeEnviada(anyLong(), any(), any(), any());
        verify(servicoRepository, times(2)).saveAndFlush(any());
    }

    @Test
    void aprovarUma_erroTecnico_ePropagado() {
        linha(1, SituacaoLinha.NOVA, null);
        when(servicoRepository.findByCodigoPadronizado(anyString()))
                .thenThrow(new CannotAcquireLockException("lock timeout"));

        assertThatThrownBy(() -> service.aprovar(1L, "gil")).isInstanceOf(CannotAcquireLockException.class);

        verify(linhaRepository, never()).marcarConflitoSeEnviada(anyLong(), any(), any(), any());
    }

    @Test
    void conflitoDeNegocio_continuaViraConflito() {
        linha(1, SituacaoLinha.NOVA, null);
        when(servicoRepository.findByCodigoPadronizado("PINT_TUB_HN4321000")).thenReturn(Optional.of(existente));
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of(1L));

        assertThat(service.aprovarEmBloco(7L, null, "gil")).isEqualTo(new ResumoAprovacao(0, 1, 0));

        assertThat(motivoConflito(1)).contains("já foi cadastrado");
    }

    @Test
    void rejeitarEmBloco_erroTecnicoNumaLinha_naoParaAsOutras() {
        when(linhaRepository.idsEnviadasDoLote(anyLong(), any(), any())).thenReturn(List.of(1L, 2L, 3L));
        when(linhaRepository.rejeitarSeEnviada(eq(1L), any(), any(), any())).thenReturn(1);
        when(linhaRepository.rejeitarSeEnviada(eq(2L), any(), any(), any()))
                .thenThrow(new CannotAcquireLockException("lock timeout"));
        when(linhaRepository.rejeitarSeEnviada(eq(3L), any(), any(), any())).thenReturn(1);

        assertThat(service.rejeitarEmBloco(7L, null, "gil", "fora")).isEqualTo(2);
    }

    @Test
    void rejeitarEmBloco_exigeMotivoEContaSoAsQueMudaram() {
        assertThatThrownBy(() -> service.rejeitarEmBloco(7L, null, "gil", ""))
                .isInstanceOf(IllegalArgumentException.class);

        when(linhaRepository.idsEnviadasDoLote(7L, Set.of(SituacaoLinha.NOVA), null)).thenReturn(List.of(1L, 2L, 3L));
        when(linhaRepository.rejeitarSeEnviada(eq(1L), any(), any(), any())).thenReturn(1);
        when(linhaRepository.rejeitarSeEnviada(eq(2L), any(), any(), any())).thenReturn(0);
        when(linhaRepository.rejeitarSeEnviada(eq(3L), any(), any(), any())).thenReturn(1);

        int n = service.rejeitarEmBloco(7L, new FiltroAprovacao(Set.of(SituacaoLinha.NOVA), null), "gil", "fora");

        assertThat(n).isEqualTo(2);
        verify(linhaRepository, times(3)).rejeitarSeEnviada(anyLong(), eq("fora"), eq("gil"), any());
    }
}
