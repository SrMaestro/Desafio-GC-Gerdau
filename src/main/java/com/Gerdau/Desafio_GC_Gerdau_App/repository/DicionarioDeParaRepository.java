package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DicionarioDeParaRepository extends JpaRepository<DicionarioDePara, Integer> {

    Optional<DicionarioDePara> findByNomeSujoDigitado(String nomeSujoDigitado);

    boolean existsByNomeSujoDigitado(String nomeSujoDigitado);
}
