package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Optional<Servico> findByCodigoPadronizado(String codigoPadronizado);

    List<Servico> findByAreaServico(String areaServico);

    List<Servico> findByCriadorCodigo(String criadorCodigo);

    List<Servico> findByAbreviatura(String abreviatura);
}