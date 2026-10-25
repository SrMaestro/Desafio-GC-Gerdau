package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LinhaImportacaoRepository extends JpaRepository<LinhaImportacao, Long> {

    List<LinhaImportacao> findByLoteIdOrderByNumero(Long loteId);

    List<LinhaImportacao> findByLoteIdAndEtapa(Long loteId, EtapaLinha etapa);

    boolean existsByCodigoGeradoAndEtapaIn(String codigoGerado, Collection<EtapaLinha> etapas);

    @Query("select l.situacao, count(l) from LinhaImportacao l where l.lote.id = :loteId group by l.situacao")
    List<Object[]> contarPorSituacao(@Param("loteId") Long loteId);

    @Query("select l.etapa, count(l) from LinhaImportacao l where l.lote.id = :loteId group by l.etapa")
    List<Object[]> contarPorEtapa(@Param("loteId") Long loteId);
}
