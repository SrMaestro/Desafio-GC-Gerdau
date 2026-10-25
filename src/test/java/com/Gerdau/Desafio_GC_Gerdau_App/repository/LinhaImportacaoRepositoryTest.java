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

        assertThat(linhaRepository.decidirSeEditavel(enviada.getId(), DecisaoParecida.MESMO_SERVICO, "ana")).isZero();
        assertThat(linhaRepository.descartarSeEditavel(enviada.getId(), "ana")).isZero();
        assertThat(linhaRepository.decidirSeEditavel(nova.getId(), DecisaoParecida.MESMO_SERVICO, "ana")).isZero();
        assertThat(linhaRepository.decidirSeEditavel(parecida.getId(), DecisaoParecida.OUTRO_SERVICO, "ana"))
                .isEqualTo(1);
        assertThat(linhaRepository.descartarSeEditavel(nova.getId(), "ana")).isEqualTo(1);

        LinhaImportacao p = linhaRepository.findById(parecida.getId()).orElseThrow();
        assertThat(p.getEtapa()).isEqualTo(EtapaLinha.PRONTA);
        assertThat(p.getDecisao()).isEqualTo(DecisaoParecida.OUTRO_SERVICO);
        assertThat(p.getConferidoPor()).isEqualTo("ana");
        assertThat(linhaRepository.findById(enviada.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.ENVIADA);
        assertThat(linhaRepository.findById(nova.getId()).orElseThrow().getEtapa()).isEqualTo(EtapaLinha.DESCARTADA);
    }
}
