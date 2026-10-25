package com.Gerdau.Desafio_GC_Gerdau_App.service;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.LinhaPlanilha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LinhaImportacaoRepository;
import com.Gerdau.Desafio_GC_Gerdau_App.repository.LoteImportacaoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Conferência do comprador sobre o lote que ele importou: corrigir linhas com erro, decidir as parecidas,
 * descartar e enviar ao aprovador. Todo método confere que o lote pertence ao usuário
 * ({@link AccessDeniedException} se não) e só mexe em linhas ainda em {@code EM_CONFERENCIA} ou {@code PRONTA}.
 * Cada método público é uma transação: falha no meio não deixa linhas pela metade.
 */
@Service
public class ConferenciaService {

    private static final Set<EtapaLinha> ETAPAS_EDITAVEIS = Set.of(EtapaLinha.EM_CONFERENCIA, EtapaLinha.PRONTA);

    private final PreparadorLinha preparador;
    private final LoteImportacaoRepository loteRepository;
    private final LinhaImportacaoRepository linhaRepository;

    ConferenciaService(PreparadorLinha preparador, LoteImportacaoRepository loteRepository,
                       LinhaImportacaoRepository linhaRepository) {
        this.preparador = preparador;
        this.loteRepository = loteRepository;
        this.linhaRepository = linhaRepository;
    }

    /** Regrava os campos da linha e a reclassifica como na importação (inclusive a repetição dentro do lote). */
    @Transactional
    public LinhaImportacao corrigir(Long linhaId, LinhaPlanilha novosDados, String usuario) {
        LinhaImportacao linha = linhaEditavel(linhaId, usuario);
        LoteImportacao lote = linha.getLote();
        Set<String> codigosJaVistos = new HashSet<>(linhaRepository.codigosGeradosDasOutrasLinhas(
                lote.getId(), linha.getId(), EtapaLinha.DESCARTADA));
        LinhaPlanilha dados = new LinhaPlanilha(linha.getNumero(), novosDados.areaServico(), novosDados.textoBreve(),
                novosDados.abreviatura(), novosDados.complemento(), novosDados.condicao(), novosDados.cnae(),
                novosDados.codigoNbs(), novosDados.criadorCodigo());
        preparador.preencher(linha, dados, lote.getEnviadoPor(), codigosJaVistos);
        linha.setDecisao(null); // a decisão valia para a classificação anterior
        linha.setConferidoPor(usuario);
        linhaRepository.save(linha);
        return linha;
    }

    @Transactional
    public void decidir(Long linhaId, DecisaoParecida decisao, String usuario) {
        if (decisao == null) {
            throw new IllegalArgumentException("Informe a decisão");
        }
        LinhaImportacao linha = linhaEditavel(linhaId, usuario);
        if (linha.getSituacao() != SituacaoLinha.PARECIDA) {
            throw new IllegalStateException("Só linhas parecidas precisam de decisão");
        }
        if (linhaRepository.decidirSeEditavel(linhaId, decisao, usuario) == 0) {
            throw linhaMudou(linha);
        }
    }

    /** Aplica a decisão às parecidas ainda sem decisão com score a partir do mínimo; devolve quantas. */
    @Transactional
    public int decidirEmBloco(Long loteId, double scoreMinimo, DecisaoParecida decisao, String usuario) {
        if (decisao == null) {
            throw new IllegalArgumentException("Informe a decisão");
        }
        carregarLoteDoUsuario(loteId, usuario);
        List<LinhaImportacao> linhas = linhaRepository.findParecidasSemDecisao(loteId, scoreMinimo);
        linhas.forEach(l -> aplicarDecisao(l, decisao, usuario));
        linhaRepository.saveAll(linhas);
        return linhas.size();
    }

    @Transactional
    public void descartar(Long linhaId, String usuario) {
        LinhaImportacao linha = linhaEditavel(linhaId, usuario);
        if (linhaRepository.descartarSeEditavel(linhaId, usuario) == 0) {
            throw linhaMudou(linha);
        }
    }

    /**
     * Passa as linhas PRONTA do lote para ENVIADA num único update guardado (atômico mesmo com chamadas
     * concorrentes); as demais ficam como estão. Devolve quantas foram enviadas agora.
     */
    @Transactional
    public int enviarAoAprovador(Long loteId, String usuario) {
        carregarLoteDoUsuario(loteId, usuario);
        return linhaRepository.marcarProntasComoEnviadas(loteId, LocalDateTime.now());
    }

    private static void aplicarDecisao(LinhaImportacao linha, DecisaoParecida decisao, String usuario) {
        linha.setDecisao(decisao);
        linha.setEtapa(EtapaLinha.PRONTA);
        linha.setConferidoPor(usuario);
    }

    private static IllegalStateException linhaMudou(LinhaImportacao linha) {
        return new IllegalStateException("A linha " + linha.getNumero()
                + " foi alterada ou enviada por outra operação e não pode mais ser modificada");
    }

    private LinhaImportacao linhaEditavel(Long linhaId, String usuario) {
        LinhaImportacao linha = linhaRepository.findById(linhaId)
                .orElseThrow(() -> new NoSuchElementException("Linha de importação não encontrada: " + linhaId));
        conferirDono(linha.getLote(), usuario);
        if (!ETAPAS_EDITAVEIS.contains(linha.getEtapa())) {
            throw new IllegalStateException("A linha " + linha.getNumero() + " não pode mais ser alterada (etapa "
                    + linha.getEtapa() + ")");
        }
        return linha;
    }

    private LoteImportacao carregarLoteDoUsuario(Long loteId, String usuario) {
        LoteImportacao lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new NoSuchElementException("Lote de importação não encontrado: " + loteId));
        conferirDono(lote, usuario);
        return lote;
    }

    private static void conferirDono(LoteImportacao lote, String usuario) {
        if (usuario == null || !usuario.equals(lote.getEnviadoPor())) {
            throw new AccessDeniedException("O lote pertence a outro usuário");
        }
    }
}
