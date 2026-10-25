package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
