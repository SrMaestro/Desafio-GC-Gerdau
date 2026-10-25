package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LinhaImportacaoRepository extends JpaRepository<LinhaImportacao, Long> {

    List<LinhaImportacao> findByLoteIdOrderByNumero(Long loteId);

    List<LinhaImportacao> findByLoteIdAndEtapa(Long loteId, EtapaLinha etapa);

    boolean existsByCodigoGeradoAndEtapaIn(String codigoGerado, Collection<EtapaLinha> etapas);

    @Query("select l.situacao, count(l) from LinhaImportacao l where l.lote.id = :loteId group by l.situacao")
    List<Object[]> contarPorSituacao(@Param("loteId") Long loteId);

    @Query("select l.etapa, count(l) from LinhaImportacao l where l.lote.id = :loteId group by l.etapa")
    List<Object[]> contarPorEtapa(@Param("loteId") Long loteId);

    /** Códigos gerados das demais linhas do lote (sem a própria e sem as descartadas), para achar repetição interna. */
    @Query("select l.codigoGerado from LinhaImportacao l where l.lote.id = :loteId and l.id <> :linhaId "
            + "and l.etapa <> :excluida and l.codigoGerado is not null")
    Set<String> codigosGeradosDasOutrasLinhas(@Param("loteId") Long loteId, @Param("linhaId") Long linhaId,
                                              @Param("excluida") EtapaLinha excluida);

    /** PARECIDA ainda sem decisão (esperando conferência) com score a partir do mínimo. */
    @Query("select l from LinhaImportacao l where l.lote.id = :loteId and l.situacao = "
            + "com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha.PARECIDA and l.etapa = "
            + "com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.EM_CONFERENCIA and l.decisao is null "
            + "and l.score >= :scoreMinimo order by l.numero")
    List<LinhaImportacao> findParecidasSemDecisao(@Param("loteId") Long loteId,
                                                  @Param("scoreMinimo") double scoreMinimo);

    // Updates guardados: o WHERE é a única defesa contra chamadas concorrentes (duplo clique, duas abas), então
    // uma linha já ENVIADA nunca é sobrescrita. Devolvem quantas linhas mudaram.

    /** PRONTA para ENVIADA em todo o lote, de uma vez; chamadas repetidas devolvem 0. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LinhaImportacao l set l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA, l.enviadoEm = :agora "
            + "where l.lote.id = :loteId and l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.PRONTA")
    int marcarProntasComoEnviadas(@Param("loteId") Long loteId, @Param("agora") LocalDateTime agora);

    /** Descarta a linha se ela ainda estiver em conferência ou pronta. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LinhaImportacao l set l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.DESCARTADA, l.conferidoPor = :usuario "
            + "where l.id = :id and l.etapa in (com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.EM_CONFERENCIA, com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.PRONTA)")
    int descartarSeEditavel(@Param("id") Long id, @Param("usuario") String usuario);

    /** Registra a decisão e deixa a linha PRONTA, se ainda for PARECIDA em conferência ou pronta. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LinhaImportacao l set l.decisao = :decisao, l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.PRONTA, l.conferidoPor = :usuario "
            + "where l.id = :id and l.situacao = com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha.PARECIDA "
            + "and l.etapa in (com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.EM_CONFERENCIA, com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.PRONTA)")
    int decidirSeEditavel(@Param("id") Long id, @Param("decisao") DecisaoParecida decisao,
                          @Param("usuario") String usuario);
    // Aprovação

    /**
     * Carrega a linha com {@code SELECT ... FOR UPDATE}: dois aprovadores na mesma linha esperam um pelo outro,
     * e quem chega depois já vê a etapa nova. Só faz sentido dentro de uma transação.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LinhaImportacao l where l.id = :id")
    Optional<LinhaImportacao> findByIdParaAprovar(@Param("id") Long id);

    /** Ids das linhas ENVIADA do lote com uma das situações e, se informado, score a partir do mínimo. */
    @Query("select l.id from LinhaImportacao l where l.lote.id = :loteId "
            + "and l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA "
            + "and l.situacao in :situacoes and (:scoreMinimo is null or l.score >= :scoreMinimo) order by l.numero")
    List<Long> idsEnviadasDoLote(@Param("loteId") Long loteId, @Param("situacoes") Collection<SituacaoLinha> situacoes,
                                 @Param("scoreMinimo") Double scoreMinimo);

    /** Marca a linha como CONFLITO com o motivo, se ela ainda estiver ENVIADA. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LinhaImportacao l set l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.CONFLITO, "
            + "l.motivo = :motivo, l.decididoPor = :usuario, l.decididoEm = :agora "
            + "where l.id = :id and l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA")
    int marcarConflitoSeEnviada(@Param("id") Long id, @Param("motivo") String motivo,
                                @Param("usuario") String usuario, @Param("agora") LocalDateTime agora);

    /** Rejeita a linha com o motivo, se ela ainda estiver ENVIADA. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update LinhaImportacao l set l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.REJEITADA, "
            + "l.motivo = :motivo, l.decididoPor = :usuario, l.decididoEm = :agora "
            + "where l.id = :id and l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA")
    int rejeitarSeEnviada(@Param("id") Long id, @Param("motivo") String motivo,
                          @Param("usuario") String usuario, @Param("agora") LocalDateTime agora);

    // Listagem paginada para a API (a ordem vem do Pageable, por numero)

    Page<LinhaImportacao> findByLoteId(Long loteId, Pageable pageable);

    Page<LinhaImportacao> findByLoteIdAndSituacao(Long loteId, SituacaoLinha situacao, Pageable pageable);

    Page<LinhaImportacao> findByLoteIdAndEtapa(Long loteId, EtapaLinha etapa, Pageable pageable);

    Page<LinhaImportacao> findByLoteIdAndSituacaoAndEtapa(Long loteId, SituacaoLinha situacao, EtapaLinha etapa,
                                                          Pageable pageable);

    /** Mesmo corte do filtro de aprovação em bloco: score a partir do mínimo (linhas sem score ficam de fora). */
    Page<LinhaImportacao> findByLoteIdAndSituacaoAndEtapaAndScoreGreaterThanEqual(Long loteId, SituacaoLinha situacao,
                                                                                 EtapaLinha etapa, double scoreMinimo,
                                                                                 Pageable pageable);

    Page<LinhaImportacao> findByLoteIdAndSituacaoAndScoreGreaterThanEqual(Long loteId, SituacaoLinha situacao,
                                                                         double scoreMinimo, Pageable pageable);

    boolean existsByLoteIdAndEtapa(Long loteId, EtapaLinha etapa);

    boolean existsByLoteIdAndEtapaIn(Long loteId, Collection<EtapaLinha> etapas);

    /**
     * Lotes com ao menos uma linha ENVIADA ou CONFLITO: {id, arquivo, enviadoPor, criadoEm, enviadas, conflitos},
     * mais novos primeiro.
     */
    @Query("select l.lote.id, l.lote.arquivoNome, l.lote.enviadoPor, l.lote.criadoEm, "
            + "sum(case when l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA then 1 else 0 end), "
            + "sum(case when l.etapa = com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.CONFLITO then 1 else 0 end) "
            + "from LinhaImportacao l "
            + "where l.etapa in (com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.ENVIADA, "
            + "com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha.CONFLITO) "
            + "group by l.lote.id, l.lote.arquivoNome, l.lote.enviadoPor, l.lote.criadoEm order by l.lote.criadoEm desc")
    List<Object[]> lotesComLinhasEnviadas();
}
