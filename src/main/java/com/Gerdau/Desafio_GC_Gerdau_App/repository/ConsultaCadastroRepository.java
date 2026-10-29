package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.ConsultaCadastro;
import com.Gerdau.Desafio_GC_Gerdau_App.model.DecisaoConsulta;
import com.Gerdau.Desafio_GC_Gerdau_App.model.ResultadoConsulta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaCadastroRepository extends JpaRepository<ConsultaCadastro, Long> {

    List<ConsultaCadastro> findTop200ByOrderByCriadoEmDesc();

    long countByResultado(ResultadoConsulta resultado);

    long countByDecisao(DecisaoConsulta decisao);

    @EntityGraph(attributePaths = "sugestoes")
    Optional<ConsultaCadastro> findComSugestoesById(Long id);
}
