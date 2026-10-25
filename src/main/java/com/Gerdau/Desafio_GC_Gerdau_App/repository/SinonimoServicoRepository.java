package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.SinonimoServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SinonimoServicoRepository extends JpaRepository<SinonimoServico, Long> {

    Optional<SinonimoServico> findByTextoNormalizadoAndCondicaoAndCnae(String textoNormalizado, String condicao, String cnae);
}
