package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoParecida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
}
