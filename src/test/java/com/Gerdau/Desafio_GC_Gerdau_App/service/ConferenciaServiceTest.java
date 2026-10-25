package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ResultadoClassificacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConferenciaServiceTest {

    @Mock ClassificadorLinha classificador;
    @Mock LoteImportacaoRepository loteRepository;
    @Mock LinhaImportacaoRepository linhaRepository;

    ConferenciaService service;
    LoteImportacao lote;

    @BeforeEach
    void setUp() {
        service = new ConferenciaService(new PreparadorLinha(classificador, linhaRepository), loteRepository,
                linhaRepository);
        lote = new LoteImportacao();
        lote.setId(7L);
        lote.setEnviadoPor("ana");
        when(loteRepository.findById(7L)).thenReturn(Optional.of(lote));
    }

    private LinhaImportacao linha(long id, SituacaoLinha situacao, EtapaLinha etapa) {
        LinhaImportacao l = new LinhaImportacao();
        l.setId(id);
        l.setLote(lote);
        l.setNumero((int) id + 1);
        l.setSituacao(situacao);
        l.setEtapa(etapa);
        when(linhaRepository.findById(id)).thenReturn(Optional.of(l));
        return l;
    }

    private static LinhaPlanilha dados() {
        return new LinhaPlanilha(99, "Manutenção", "Pintura de tubulação", "PINT", "TUB", "hora normal", "4321",
                null, null);
    }

    @Test
    void corrigir_erroDeCondicao_reclassificaParaNova() {
        LinhaImportacao l = linha(1, SituacaoLinha.ERRO, EtapaLinha.EM_CONFERENCIA);
        l.setMotivo("Condição inválida");
        when(linhaRepository.codigosGeradosDasOutrasLinhas(7L, 1L, EtapaLinha.DESCARTADA))
                .thenReturn(Set.of("OUTRO_1"));
        when(classificador.classificar(any(), anySet())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.NOVA, "PINTTUB_HN4321", null, null, null));

        LinhaImportacao r = service.corrigir(1L, dados(), "ana");

        ArgumentCaptor<Set<String>> vistos = ArgumentCaptor.forClass(Set.class);
        verify(classificador).classificar(any(), vistos.capture());
        assertThat(vistos.getValue()).contains("OUTRO_1");
        assertThat(r.getSituacao()).isEqualTo(SituacaoLinha.NOVA);
        assertThat(r.getEtapa()).isEqualTo(EtapaLinha.PRONTA);
        assertThat(r.getCodigoGerado()).isEqualTo("PINTTUB_HN4321");
        assertThat(r.getCondicao()).isEqualTo("hora normal");
        assertThat(r.getNumero()).isEqualTo(2); // mantém a posição original na planilha
        assertThat(r.getConferidoPor()).isEqualTo("ana");
        verify(linhaRepository).save(l);
    }

    @Test
    void corrigir_continuaComErro_mantemEmConferencia() {
        linha(1, SituacaoLinha.ERRO, EtapaLinha.EM_CONFERENCIA);
        when(classificador.classificar(any(), anySet())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.ERRO, null, null, null, "Condição inválida"));

        LinhaImportacao r = service.corrigir(1L, dados(), "ana");

        assertThat(r.getSituacao()).isEqualTo(SituacaoLinha.ERRO);
        assertThat(r.getEtapa()).isEqualTo(EtapaLinha.EM_CONFERENCIA);
        assertThat(r.getMotivo()).isEqualTo("Condição inválida");
    }

    @Test
    void corrigir_codigoJaEnviadoEmOutroLote_viraRepetida() {
        linha(1, SituacaoLinha.ERRO, EtapaLinha.EM_CONFERENCIA);
        when(classificador.classificar(any(), anySet())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.NOVA, "X_HN1", null, null, null));
        when(linhaRepository.existsByCodigoGeradoAndEtapaIn(eq("X_HN1"), anyCollection())).thenReturn(true);

        LinhaImportacao r = service.corrigir(1L, dados(), "ana");

        assertThat(r.getSituacao()).isEqualTo(SituacaoLinha.REPETIDA);
        assertThat(r.getEtapa()).isEqualTo(EtapaLinha.IGNORADA);
    }

    @Test
    void corrigir_linhaJaEnviada_recusa() {
        linha(1, SituacaoLinha.NOVA, EtapaLinha.ENVIADA);

        assertThatThrownBy(() -> service.corrigir(1L, dados(), "ana")).isInstanceOf(IllegalStateException.class);
        verify(linhaRepository, never()).save(any());
    }

    @Test
    void corrigir_limpaDecisaoAnterior() {
        LinhaImportacao l = linha(1, SituacaoLinha.PARECIDA, EtapaLinha.PRONTA);
        l.setDecisao(DecisaoParecida.OUTRO_SERVICO);
        when(classificador.classificar(any(), anySet())).thenReturn(
                new ResultadoClassificacao(SituacaoLinha.PARECIDA, "X_HN1", 5, 0.8, "parecido"));

        LinhaImportacao r = service.corrigir(1L, dados(), "ana");

        assertThat(r.getDecisao()).isNull();
        assertThat(r.getEtapa()).isEqualTo(EtapaLinha.EM_CONFERENCIA);
    }

    @Test
    void decidir_somenteEmParecida() {
        linha(1, SituacaoLinha.NOVA, EtapaLinha.PRONTA);
        LinhaImportacao p = linha(2, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);
        linha(3, SituacaoLinha.PARECIDA, EtapaLinha.ENVIADA);

        assertThatThrownBy(() -> service.decidir(1L, DecisaoParecida.OUTRO_SERVICO, "ana"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.decidir(3L, DecisaoParecida.OUTRO_SERVICO, "ana"))
                .isInstanceOf(IllegalStateException.class);

        service.decidir(2L, DecisaoParecida.MESMO_SERVICO, "ana");

        assertThat(p.getDecisao()).isEqualTo(DecisaoParecida.MESMO_SERVICO);
        assertThat(p.getEtapa()).isEqualTo(EtapaLinha.PRONTA);
        assertThat(p.getConferidoPor()).isEqualTo("ana");
        verify(linhaRepository).save(p);
    }

    @Test
    void decidirEmBloco_respeitaScoreMinimo() {
        LinhaImportacao a = linha(1, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);
        LinhaImportacao b = linha(2, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);
        when(linhaRepository.findParecidasSemDecisao(7L, 0.9)).thenReturn(List.of(a, b));

        int n = service.decidirEmBloco(7L, 0.9, DecisaoParecida.OUTRO_SERVICO, "ana");

        assertThat(n).isEqualTo(2);
        assertThat(a.getDecisao()).isEqualTo(DecisaoParecida.OUTRO_SERVICO);
        assertThat(b.getEtapa()).isEqualTo(EtapaLinha.PRONTA);
        assertThat(b.getConferidoPor()).isEqualTo("ana");
        verify(linhaRepository).saveAll(List.of(a, b));
    }

    @Test
    void enviar_soPronta() {
        LinhaImportacao nova = linha(1, SituacaoLinha.NOVA, EtapaLinha.PRONTA);
        LinhaImportacao decidida = linha(2, SituacaoLinha.PARECIDA, EtapaLinha.PRONTA);
        // o repositório só devolve PRONTA: ERRO e PARECIDA sem decisão (EM_CONFERENCIA) nunca entram
        when(linhaRepository.findByLoteIdAndEtapa(7L, EtapaLinha.PRONTA)).thenReturn(List.of(nova, decidida));
        LinhaImportacao erro = linha(3, SituacaoLinha.ERRO, EtapaLinha.EM_CONFERENCIA);
        LinhaImportacao semDecisao = linha(4, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);

        int n = service.enviarAoAprovador(7L, "ana");

        assertThat(n).isEqualTo(2);
        assertThat(nova.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(nova.getEnviadoEm()).isNotNull();
        assertThat(decidida.getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(erro.getEtapa()).isEqualTo(EtapaLinha.EM_CONFERENCIA);
        assertThat(erro.getEnviadoEm()).isNull();
        assertThat(semDecisao.getEtapa()).isEqualTo(EtapaLinha.EM_CONFERENCIA);
    }

    @Test
    void enviar_duasVezes_naoReenvia() {
        LinhaImportacao nova = linha(1, SituacaoLinha.NOVA, EtapaLinha.PRONTA);
        when(linhaRepository.findByLoteIdAndEtapa(7L, EtapaLinha.PRONTA)).thenReturn(List.of(nova))
                .thenReturn(List.of());

        assertThat(service.enviarAoAprovador(7L, "ana")).isEqualTo(1);
        var primeiroEnvio = nova.getEnviadoEm();
        assertThat(service.enviarAoAprovador(7L, "ana")).isZero();
        assertThat(nova.getEnviadoEm()).isEqualTo(primeiroEnvio);
    }

    @Test
    void descartar_naoVaiNoEnvio() {
        LinhaImportacao l = linha(1, SituacaoLinha.NOVA, EtapaLinha.PRONTA);
        when(linhaRepository.findByLoteIdAndEtapa(7L, EtapaLinha.PRONTA)).thenAnswer(i ->
                l.getEtapa() == EtapaLinha.PRONTA ? List.of(l) : List.of());

        service.descartar(1L, "ana");
        int n = service.enviarAoAprovador(7L, "ana");

        assertThat(l.getEtapa()).isEqualTo(EtapaLinha.DESCARTADA);
        assertThat(l.getConferidoPor()).isEqualTo("ana");
        assertThat(n).isZero();
    }

    @Test
    void descartar_linhaJaEnviada_recusa() {
        linha(1, SituacaoLinha.NOVA, EtapaLinha.ENVIADA);

        assertThatThrownBy(() -> service.descartar(1L, "ana")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void outroUsuario_recebeAccessDenied() {
        linha(1, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);

        assertThatThrownBy(() -> service.corrigir(1L, dados(), "bia")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.decidir(1L, DecisaoParecida.MESMO_SERVICO, "bia"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.decidirEmBloco(7L, 0.5, DecisaoParecida.MESMO_SERVICO, "bia"))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.descartar(1L, "bia")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.enviarAoAprovador(7L, "bia")).isInstanceOf(AccessDeniedException.class);
        verify(linhaRepository, never()).save(any());
        verify(linhaRepository, never()).saveAll(any());
    }

    @Test
    void linhaOuLoteInexistente_lancaNoSuchElement() {
        when(linhaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.descartar(404L, "ana")).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(() -> service.enviarAoAprovador(404L, "ana")).isInstanceOf(NoSuchElementException.class);
    }
}
