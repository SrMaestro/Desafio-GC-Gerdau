package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.LinhaImportacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LinhaImportacaoRepository extends JpaRepository<LinhaImportacao, Long> {

    List<LinhaImportacao> findByLoteIdOrderByNumero(Long loteId);

    List<LinhaImportacao> findByLoteIdAndEtapa(Long loteId, EtapaLinha etapa);

    boolean existsByCodigoGeradoAndEtapaIn(String codigoGerado, Collection<EtapaLinha> etapas);
}
