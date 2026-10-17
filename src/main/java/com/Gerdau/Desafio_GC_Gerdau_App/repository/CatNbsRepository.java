package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.dto.NbsSimilar;
import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatNbsRepository extends JpaRepository<CatNbs, String> {

    @Query(value = """
            SELECT codigo_nbs AS codigoNbs, descricao_atividade AS descricao,
                   similarity(lower(descricao_atividade), lower(:texto)) AS score
            FROM cat_nbs
            WHERE similarity(lower(descricao_atividade), lower(:texto)) >= :minimo
            ORDER BY score DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<NbsSimilar> buscarSugestoes(@Param("texto") String texto,
                                     @Param("minimo") double minimo,
                                     @Param("limite") int limite);
}
