package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Usa o Postgres do compose; o {@code @Transactional} desfaz tudo ao fim de cada teste. */
@SpringBootTest
@Transactional
class LinhaImportacaoRepositoryTest {

    @Autowired LoteImportacaoRepository loteRepository;
    @Autowired LinhaImportacaoRepository linhaRepository;

    private LoteImportacao lote() {
        LoteImportacao l = new LoteImportacao();
        l.setArquivoNome("teste.csv");
        l.setEnviadoPor("teste-repo");
        l.setStatus(StatusLote.CONCLUIDO);
        l.setTotalLinhas(4);
        return loteRepository.save(l);
    }

    private LinhaImportacao linha(LoteImportacao lote, int n, SituacaoLinha s, EtapaLinha e) {
        LinhaImportacao l = new LinhaImportacao();
        l.setLote(lote);
        l.setNumero(n);
        l.setSituacao(s);
        l.setEtapa(e);
        return linhaRepository.save(l);
    }

    @Test
    void marcarProntasComoEnviadas_soPronta_eSegundaVezDevolveZero() {
        LoteImportacao lote = lote();
        LinhaImportacao a = linha(lote, 2, SituacaoLinha.NOVA, EtapaLinha.PRONTA);
        LinhaImportacao b = linha(lote, 3, SituacaoLinha.PARECIDA, EtapaLinha.PRONTA);
        LinhaImportacao c = linha(lote, 4, SituacaoLinha.ERRO, EtapaLinha.EM_CONFERENCIA);
        LinhaImportacao d = linha(lote, 5, SituacaoLinha.NOVA, EtapaLinha.DESCARTADA);

        assertThat(linhaRepository.marcarProntasComoEnviadas(lote.getId(), LocalDateTime.now())).isEqualTo(2);
        assertThat(linhaRepository.marcarProntasComoEnviadas(lote.getId(), LocalDateTime.now())).isZero();

        assertThat(linhaRepository.findById(a.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(linhaRepository.findById(a.getId()).orElseThrow().getEnviadoEm()).isNotNull();
        assertThat(linhaRepository.findById(b.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(linhaRepository.findById(c.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.EM_CONFERENCIA);
        assertThat(linhaRepository.findById(d.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.DESCARTADA);
    }

    @Test
    void descartarEDecidir_naoSobrescrevemLinhaEnviada() {
        LoteImportacao lote = lote();
        LinhaImportacao parecida = linha(lote, 2, SituacaoLinha.PARECIDA, EtapaLinha.EM_CONFERENCIA);
        LinhaImportacao enviada = linha(lote, 3, SituacaoLinha.PARECIDA, EtapaLinha.ENVIADA);
        LinhaImportacao nova = linha(lote, 4, SituacaoLinha.NOVA, EtapaLinha.PRONTA);

        LocalDateTime agora = LocalDateTime.of(2026, 10, 25, 9, 30);
        assertThat(linhaRepository.decidirSeEditavel(enviada.getId(), DecisaoParecida.MESMO_SERVICO, "ana", agora)).isZero();
        assertThat(linhaRepository.descartarSeEditavel(enviada.getId(), "ana", agora)).isZero();
        assertThat(linhaRepository.decidirSeEditavel(nova.getId(), DecisaoParecida.MESMO_SERVICO, "ana", agora)).isZero();
        assertThat(linhaRepository.decidirSeEditavel(parecida.getId(), DecisaoParecida.OUTRO_SERVICO, "ana", agora))
                .isEqualTo(1);
        assertThat(linhaRepository.descartarSeEditavel(nova.getId(), "ana", agora)).isEqualTo(1);

        LinhaImportacao p = linhaRepository.findById(parecida.getId()).orElseThrow();
        assertThat(p.getEtapa()).isEqualTo(EtapaLinha.PRONTA);
        assertThat(p.getDecisao()).isEqualTo(DecisaoParecida.OUTRO_SERVICO);
        assertThat(p.getConferidoEm()).isEqualTo(agora);
        assertThat(linhaRepository.findById(nova.getId()).orElseThrow().getConferidoEm()).isEqualTo(agora);
        assertThat(linhaRepository.findById(enviada.getId()).orElseThrow().getConferidoEm()).isNull();
        assertThat(p.getConferidoPor()).isEqualTo("ana");
        assertThat(linhaRepository.findById(enviada.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(linhaRepository.findById(nova.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.DESCARTADA);
    }
    private LinhaImportacao linha(LoteImportacao lote, int n, SituacaoLinha s, EtapaLinha e, Double score) {
        LinhaImportacao l = new LinhaImportacao();
        l.setLote(lote);
        l.setNumero(n);
        l.setSituacao(s);
        l.setEtapa(e);
        l.setScore(score);
        return linhaRepository.save(l);
    }

    @Test
    void idsEnviadasDoLote_soEnviadasDoLote_filtraSituacaoEScore() {
        LoteImportacao lote = lote();
        LoteImportacao outro = lote();
        LinhaImportacao nova = linha(lote, 2, SituacaoLinha.NOVA, EtapaLinha.ENVIADA, null);
        LinhaImportacao alta = linha(lote, 3, SituacaoLinha.PARECIDA, EtapaLinha.ENVIADA, 0.9);
        LinhaImportacao baixa = linha(lote, 4, SituacaoLinha.PARECIDA, EtapaLinha.ENVIADA, 0.5);
        linha(lote, 5, SituacaoLinha.NOVA, EtapaLinha.PRONTA, null);
        linha(lote, 6, SituacaoLinha.NOVA, EtapaLinha.APROVADA, null);
        linha(lote, 7, SituacaoLinha.PARECIDA, EtapaLinha.CONFLITO, 0.95);
        linha(outro, 2, SituacaoLinha.NOVA, EtapaLinha.ENVIADA, null);

        assertThat(linhaRepository.idsEnviadasDoLote(lote.getId(), EnumSet.allOf(SituacaoLinha.class), null))
                .containsExactly(nova.getId(), alta.getId(), baixa.getId());
        assertThat(linhaRepository.idsEnviadasDoLote(lote.getId(), Set.of(SituacaoLinha.NOVA), null))
                .containsExactly(nova.getId());
        assertThat(linhaRepository.idsEnviadasDoLote(lote.getId(), Set.of(SituacaoLinha.PARECIDA), 0.85))
                .containsExactly(alta.getId());
    }

    @Test
    void findByIdParaAprovar_devolveALinha() {
        LinhaImportacao l = linha(lote(), 2, SituacaoLinha.NOVA, EtapaLinha.ENVIADA);

        assertThat(linhaRepository.findByIdParaAprovar(l.getId())).get()
                .extracting(LinhaImportacao::getId).isEqualTo(l.getId());
    }

    @Test
    void conflitoERejeicao_soMudamLinhaEnviada() {
        LoteImportacao lote = lote();
        LinhaImportacao a = linha(lote, 2, SituacaoLinha.NOVA, EtapaLinha.ENVIADA);
        LinhaImportacao b = linha(lote, 3, SituacaoLinha.NOVA, EtapaLinha.ENVIADA);
        LinhaImportacao aprovada = linha(lote, 4, SituacaoLinha.NOVA, EtapaLinha.APROVADA);
        LocalDateTime agora = LocalDateTime.now();

        assertThat(linhaRepository.marcarConflitoSeEnviada(a.getId(), "código ocupado", "gil", agora)).isEqualTo(1);
        assertThat(linhaRepository.marcarConflitoSeEnviada(a.getId(), "de novo", "gil", agora)).isZero();
        assertThat(linhaRepository.rejeitarSeEnviada(b.getId(), "genérico", "gil", agora)).isEqualTo(1);
        assertThat(linhaRepository.rejeitarSeEnviada(b.getId(), "de novo", "gil", agora)).isZero();
        assertThat(linhaRepository.rejeitarSeEnviada(aprovada.getId(), "x", "gil", agora)).isZero();
        assertThat(linhaRepository.marcarConflitoSeEnviada(aprovada.getId(), "x", "gil", agora)).isZero();

        LinhaImportacao ca = linhaRepository.findById(a.getId()).orElseThrow();
        assertThat(ca.getEtapa()).isEqualTo(EtapaLinha.CONFLITO);
        assertThat(ca.getMotivo()).isEqualTo("código ocupado");
        assertThat(ca.getDecididoPor()).isEqualTo("gil");
        assertThat(ca.getDecididoEm()).isNotNull();
        LinhaImportacao rb = linhaRepository.findById(b.getId()).orElseThrow();
        assertThat(rb.getEtapa()).isEqualTo(EtapaLinha.REJEITADA);
        assertThat(rb.getMotivo()).isEqualTo("genérico");
        assertThat(rb.getDecididoPor()).isEqualTo("gil");
        assertThat(linhaRepository.findById(aprovada.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.APROVADA);
    }
}
