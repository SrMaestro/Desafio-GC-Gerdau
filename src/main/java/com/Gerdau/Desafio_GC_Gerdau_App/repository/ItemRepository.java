package com.Gerdau.Desafio_GC_Gerdau_App.repository;
import java.util.Optional;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByCodigoProduto(String codigoProduto);

    Optional<Item> findByCodigoServico(String codigoServico);
}