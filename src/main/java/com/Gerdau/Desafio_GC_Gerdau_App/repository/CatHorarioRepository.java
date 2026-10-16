package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.CatHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatHorarioRepository extends JpaRepository<CatHorario, Integer> {

    Optional<CatHorario> findBySufixoCodigo(String sufixoCodigo);

    List<CatHorario> findAllByOrderByDescricaoHorarioAsc();
}
