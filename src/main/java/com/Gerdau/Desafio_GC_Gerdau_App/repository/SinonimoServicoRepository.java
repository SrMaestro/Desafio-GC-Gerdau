package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.SinonimoServico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SinonimoServicoRepository extends JpaRepository<SinonimoServico, Long> {

    /** Traz o serviço junto: sem sessão aberta fora do serviço (open-in-view=false), um proxy preguiçoso estouraria. */
    @EntityGraph(attributePaths = "servico")
    Optional<SinonimoServico> findByTextoNormalizadoAndCondicaoAndCnae(String textoNormalizado, String condicao, String cnae);
}
