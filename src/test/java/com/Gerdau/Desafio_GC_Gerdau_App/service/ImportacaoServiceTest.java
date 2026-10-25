package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoClassificacao;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResumoLote;
import com.Gerdau.Desafio_GC_Gerdau_App.exception.ArquivoInvalidoException;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportacaoServiceTest {

    @Mock LeitorPlanilha leitor;
    @Mock ClassificadorLinha classificador;
    @Mock LoteImportacaoRepository loteRepository;
    @Mock LinhaImportacaoRepository linhaRepository;

    List<Runnable> fila = new ArrayList<>();
    Executor executorFalso = fila::add;
    ImportacaoService service;
    LoteImportacao lote;

    @BeforeEach
    void setUp() {
        service = new ImportacaoService(leitor, new PreparadorLinha(classificador, linhaRepository), loteRepository, linhaRepository, executorFalso);
        lote = new LoteImportacao();
        lote.setId(7L);
        lote.setArquivoNome("a.csv");
        lote.setEnviadoPor("ana");
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote));
        when(loteRepository.save(any(LoteImportacao.class))).thenAnswer(i -> {
            LoteImportacao l = i.getArgument(0);
            if (l.getId() == null) {
                l.setId(7L);
            }
            return l;
        });
        when(linhaRepository.saveAll(anyCollection())).thenAnswer(i -> i.getArgument(0));
        when(linhaRepository.existsByCodigoGeradoAndEtapaIn(anyString(), anyCollection())).thenReturn(false);
    }

    private LinhaPlanilha linha(int numero, String criador) {
        return new LinhaPlanilha(numero, "Obras", "Montagem", "AB", "_x", "Hora normal", "1234567", null, criador);
    }

    private void resultado(SituacaoLinha s, String codigo) {
        when(classificador.classificar(any(), any())).thenReturn(new ResultadoClassificacao(s, codigo, null, null, "m"));
    }

    @SuppressWarnings("unchecked")
    private List<LinhaImportacao> linhasSalvas() {
        ArgumentCaptor<Collection<LinhaImportacao>> c = ArgumentCaptor.forClass(Collection.class);
        verify(linhaRepository, org.mockito.Mockito.atLeastOnce()).saveAll(c.capture());
        List<LinhaImportacao> todas = new ArrayList<>();
        c.getAllValues().forEach(todas::addAll);
        return todas;
    }

    @Test
    void processar_classificaECriaLinhasComEtapaInicial() {
        SituacaoLinha[] ordem = {SituacaoLinha.ERRO, SituacaoLinha.PARECIDA, SituacaoLinha.NOVA,
                SituacaoLinha.EXISTENTE, SituacaoLinha.REPETIDA};
        List<ResultadoClassificacao> rs = new ArrayList<>();
        for (int i = 0; i < ordem.length; i++) {
            rs.add(new ResultadoClassificacao(ordem[i], "C" + i, null, 0.5, "m" + i));
        }
        when(classificador.classificar(any(), any())).thenReturn(rs.get(0), rs.get(1), rs.get(2), rs.get(3), rs.get(4));
        List<LinhaPlanilha> linhas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            linhas.add(linha(i + 2, i == 0 ? "bia" : null));
        }

        service.processar(7L, linhas);

        List<LinhaImportacao> salvas = linhasSalvas();
        assertThat(salvas).extracting(LinhaImportacao::getEtapa).containsExactly(
                EtapaLinha.EM_CONFERENCIA, EtapaLinha.EM_CONFERENCIA, EtapaLinha.PRONTA,
                EtapaLinha.IGNORADA, EtapaLinha.IGNORADA);
        assertThat(salvas).extracting(LinhaImportacao::getSituacao).containsExactly(ordem);
        assertThat(salvas.get(0).getNumero()).isEqualTo(2);
        assertThat(salvas.get(2).getCodigoGerado()).isEqualTo("C2");
        assertThat(salvas.get(2).getTextoBreve()).isEqualTo("Montagem");
        assertThat(salvas.get(2).getMotivo()).isEqualTo("m2");
        assertThat(salvas.get(0).getCriadorCodigo()).isEqualTo("bia");
        assertThat(salvas.get(1).getCriadorCodigo()).isEqualTo("ana");
        assertThat(salvas.get(0).getLote()).isSameAs(lote);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.CONCLUIDO);
        assertThat(lote.getProcessadas()).isEqualTo(5);
    }

    @Test
    void processar_atualizaProgresso() {
        resultado(SituacaoLinha.NOVA, null);
        List<LinhaPlanilha> linhas = new ArrayList<>();
        for (int i = 0; i < ImportacaoService.TAMANHO_BLOCO + 1; i++) {
            linhas.add(linha(i + 2, null));
        }
        List<Integer> progresso = new ArrayList<>();
        when(loteRepository.save(any(LoteImportacao.class))).thenAnswer(i -> {
            LoteImportacao l = i.getArgument(0);
            progresso.add(l.getProcessadas());
            return l;
        });

        service.processar(7L, linhas);

        verify(linhaRepository, times(2)).saveAll(anyCollection());
        assertThat(progresso).containsExactly(ImportacaoService.TAMANHO_BLOCO, ImportacaoService.TAMANHO_BLOCO + 1,
                ImportacaoService.TAMANHO_BLOCO + 1);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.CONCLUIDO);
    }

    @Test
    void processar_repeteCodigoNoMesmoArquivo() {
        // o classificador de verdade devolve REPETIDA quando o código já está em codigosJaVistos
        when(classificador.classificar(any(), any())).thenAnswer(i -> {
            Set<String> vistos = i.getArgument(1);
            return vistos.contains("X1")
                    ? new ResultadoClassificacao(SituacaoLinha.REPETIDA, "X1", null, null, "repetida")
                    : new ResultadoClassificacao(SituacaoLinha.NOVA, "X1", null, null, null);
        });

        service.processar(7L, List.of(linha(2, null), linha(3, null)));

        List<LinhaImportacao> salvas = linhasSalvas();
        assertThat(salvas.get(0).getSituacao()).isEqualTo(SituacaoLinha.NOVA);
        assertThat(salvas.get(1).getSituacao()).isEqualTo(SituacaoLinha.REPETIDA);
        assertThat(salvas.get(1).getEtapa()).isEqualTo(EtapaLinha.IGNORADA);
    }

    @Test
    void processar_codigoJaPendenteEmOutroLote_eRepetida() {
        resultado(SituacaoLinha.NOVA, "X1");
        when(linhaRepository.existsByCodigoGeradoAndEtapaIn(eq("X1"), eq(Set.of(EtapaLinha.ENVIADA)))).thenReturn(true);

        service.processar(7L, List.of(linha(2, null)));

        LinhaImportacao l = linhasSalvas().get(0);
        assertThat(l.getSituacao()).isEqualTo(SituacaoLinha.REPETIDA);
        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.IGNORADA);
        assertThat(l.getMotivo()).contains("outro lote");
    }

    @Test
    void processar_erroInesperado_marcaLoteFalhou() {
        // 1ª chamada ok, 2ª falha de banco: o que já foi gravado fica
        when(classificador.classificar(any(), any()))
                .thenReturn(new ResultadoClassificacao(SituacaoLinha.NOVA, "A", null, null, null))
                .thenThrow(new DataAccessResourceFailureException("banco fora"));
        List<LinhaPlanilha> linhas = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            linhas.add(linha(i + 2, null));
        }

        service.processar(7L, linhas);

        assertThat(lote.getStatus()).isEqualTo(StatusLote.FALHOU);
        assertThat(lote.getMensagemErro()).isNotBlank();
    }

    @Test
    void processar_errorNaClassificacao_marcaFalhouERepropaga() {
        lote.setStatus(StatusLote.PROCESSANDO);
        when(classificador.classificar(any(), any())).thenThrow(new StackOverflowError("simulado"));

        assertThatThrownBy(() -> service.processar(7L, List.of(linha(2, null))))
                .isInstanceOf(StackOverflowError.class);

        assertThat(lote.getStatus()).isEqualTo(StatusLote.FALHOU);
        assertThat(lote.getMensagemErro()).isNotBlank();
        verify(loteRepository, org.mockito.Mockito.atLeastOnce()).save(lote);
    }

    @Test
    void processar_runtimeExceptionNaClassificacao_marcaFalhou() {
        lote.setStatus(StatusLote.PROCESSANDO);
        when(classificador.classificar(any(), any())).thenThrow(new IllegalStateException("simulado"));

        service.processar(7L, List.of(linha(2, null)));

        assertThat(lote.getStatus()).isEqualTo(StatusLote.FALHOU);
    }

    @Test
    void iniciar_arquivoInvalido_naoCriaLote() {
        when(leitor.ler(any(), any())).thenThrow(new ArquivoInvalidoException("ruim"));

        assertThatThrownBy(() -> service.iniciar("a.pdf", new byte[0], "ana"))
                .isInstanceOf(ArquivoInvalidoException.class);

        verify(loteRepository, never()).save(any());
        assertThat(fila).isEmpty();
    }

    @Test
    void iniciar_criadorEmBrancoAssumeUsuario() {
        resultado(SituacaoLinha.NOVA, "A");
        when(leitor.ler(any(), any())).thenReturn(List.of(linha(2, "  ")));

        Long id = service.iniciar("a.csv", new byte[]{1}, "ana");
        fila.forEach(Runnable::run);

        assertThat(id).isEqualTo(7L);
        assertThat(linhasSalvas().get(0).getCriadorCodigo()).isEqualTo("ana");
    }

    @Test
    void iniciar_naoEsperaOProcessamento() {
        resultado(SituacaoLinha.NOVA, "A");
        when(leitor.ler(any(), any())).thenReturn(List.of(linha(2, null), linha(3, null)));

        Long id = service.iniciar("a.csv", new byte[]{1}, "ana");

        // lote gravado como PROCESSANDO e nada processado ainda: o trabalho só foi entregue ao executor
        assertThat(id).isEqualTo(7L);
        ArgumentCaptor<LoteImportacao> c = ArgumentCaptor.forClass(LoteImportacao.class);
        verify(loteRepository).save(c.capture());
        LoteImportacao criado = c.getValue();
        assertThat(criado.getStatus()).isEqualTo(StatusLote.PROCESSANDO);
        assertThat(criado.getTotalLinhas()).isEqualTo(2);
        assertThat(criado.getEnviadoPor()).isEqualTo("ana");
        assertThat(criado.getArquivoNome()).isEqualTo("a.csv");
        assertThat(fila).hasSize(1);
        verify(linhaRepository, never()).saveAll(anyCollection());
        verify(classificador, never()).classificar(any(), any());

        fila.get(0).run();
        assertThat(lote.getStatus()).isEqualTo(StatusLote.CONCLUIDO);
    }

    @Test
    void iniciar_executorRecusa_marcaLoteFalhou() {
        service = new ImportacaoService(leitor, new PreparadorLinha(classificador, linhaRepository), loteRepository, linhaRepository, r -> {
            throw new java.util.concurrent.RejectedExecutionException("cheio");
        });
        when(leitor.ler(any(), any())).thenReturn(List.of(linha(2, null)));

        service.iniciar("a.csv", new byte[]{1}, "ana");

        assertThat(lote.getStatus()).isEqualTo(StatusLote.FALHOU);
        assertThat(lote.getMensagemErro()).isNotBlank();
    }

    @Test
    void aoSubir_lotesProcessandoViramFalhou() {
        LoteImportacao outro = new LoteImportacao();
        outro.setId(8L);
        when(loteRepository.findByStatus(StatusLote.PROCESSANDO)).thenReturn(List.of(lote, outro));

        service.aoSubir();

        assertThat(lote.getStatus()).isEqualTo(StatusLote.FALHOU);
        assertThat(outro.getStatus()).isEqualTo(StatusLote.FALHOU);
        assertThat(outro.getMensagemErro()).contains("reiniciada");
    }

    @Test
    void resumo_contaPorSituacaoEEtapa() {
        lote.setTotalLinhas(10);
        lote.setProcessadas(10);
        lote.setStatus(StatusLote.CONCLUIDO);
        when(linhaRepository.contarPorSituacao(7L)).thenReturn(List.of(
                new Object[]{SituacaoLinha.NOVA, 6L}, new Object[]{SituacaoLinha.ERRO, 4L}));
        when(linhaRepository.contarPorEtapa(7L)).thenReturn(List.of(
                new Object[]{EtapaLinha.PRONTA, 6L}, new Object[]{EtapaLinha.EM_CONFERENCIA, 4L}));

        ResumoLote r = service.resumo(7L);

        assertThat(r.id()).isEqualTo(7L);
        assertThat(r.arquivo()).isEqualTo("a.csv");
        assertThat(r.status()).isEqualTo(StatusLote.CONCLUIDO);
        assertThat(r.total()).isEqualTo(10);
        assertThat(r.processadas()).isEqualTo(10);
        assertThat(r.porSituacao()).containsEntry(SituacaoLinha.NOVA, 6L).containsEntry(SituacaoLinha.ERRO, 4L);
        assertThat(r.porEtapa()).containsEntry(EtapaLinha.PRONTA, 6L).containsEntry(EtapaLinha.EM_CONFERENCIA, 4L);
    }

    @Test
    void linhaComCodigoGeradoAcimaDe40_naoEstouraAColuna() {
        String codigo = "C".repeat(41);
        when(classificador.classificar(any(), any())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.ERRO, codigo, null, null, "Código gerado passa de 40: " + codigo));

        service.processar(7L, List.of(linha(2, null)));

        LinhaImportacao l = linhasSalvas().get(0);
        assertThat(l.getCodigoGerado()).isNull();
        assertThat(l.getMotivo()).contains(codigo);
    }

    @Test
    void linhaComCamposLongos_sao_truncadosNoTamanhoDaColuna() {
        String longo = "x".repeat(300);
        resultado(SituacaoLinha.ERRO, "OK");
        when(classificador.classificar(any(), any())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.ERRO, "OK", null, null, longo + longo));

        service.processar(7L, List.of(new LinhaPlanilha(2, longo, longo, longo, longo, longo, longo, longo, longo)));

        LinhaImportacao l = linhasSalvas().get(0);
        assertThat(l.getAreaServico()).hasSize(50);
        assertThat(l.getTextoBreve()).hasSize(255);
        assertThat(l.getAbreviatura()).hasSize(10);
        assertThat(l.getComplemento()).hasSize(15);
        assertThat(l.getCondicao()).hasSize(50);
        assertThat(l.getCnae()).hasSize(15);
        assertThat(l.getCodigoNbs()).hasSize(20);
        assertThat(l.getCriadorCodigo()).hasSize(100);
        assertThat(l.getMotivo()).hasSize(500);
    }

    @Test
    void nomeDeArquivoLongo_ficaDentroDaColuna() {
        resultado(SituacaoLinha.NOVA, "A");
        when(leitor.ler(any(), any())).thenReturn(List.of(linha(2, null)));

        service.iniciar("a".repeat(300) + ".csv", new byte[]{1}, "ana");

        ArgumentCaptor<LoteImportacao> c = ArgumentCaptor.forClass(LoteImportacao.class);
        verify(loteRepository).save(c.capture());
        assertThat(c.getValue().getArquivoNome()).hasSize(255).endsWith(".csv");
    }

    @Test
    void linhaRuimNaoDerrubaALinhasBoasDoMesmoChunk() {
        when(classificador.classificar(any(), any())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.NOVA, "A", null, null, null),
                new ResultadoClassificacao(SituacaoLinha.ERRO, "C".repeat(50), null, null, "longo"),
                new ResultadoClassificacao(SituacaoLinha.NOVA, "B", null, null, null));

        service.processar(7L, List.of(linha(2, null), linha(3, null), linha(4, null)));

        assertThat(linhasSalvas()).hasSize(3);
        assertThat(lote.getStatus()).isEqualTo(StatusLote.CONCLUIDO);
    }
}
