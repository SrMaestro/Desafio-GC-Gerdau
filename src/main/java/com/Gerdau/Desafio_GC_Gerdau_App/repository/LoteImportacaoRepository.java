package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.LoteImportacao;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoteImportacaoRepository extends JpaRepository<LoteImportacao, Long> {

    List<LoteImportacao> findByStatus(StatusLote status);
}
