package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDeParaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DicionarioDeParaHistoricoRepository extends JpaRepository<DicionarioDeParaHistorico, Long> {

    List<DicionarioDeParaHistorico> findByDeParaIdOrderByEmAsc(Long deParaId);
}
