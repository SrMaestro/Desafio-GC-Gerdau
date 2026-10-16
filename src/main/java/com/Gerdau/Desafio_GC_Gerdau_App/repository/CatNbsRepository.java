package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.CatNbs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatNbsRepository extends JpaRepository<CatNbs, String> {

    interface SugestaoNbs {
        String getCodigoNbs();

        String getDescricaoAtividade();

        Double getScore();
    }

    // Busca fuzzy com pg_trgm: "<->" é a distância de trigramas; "similarity" vira o score (0 a 1).
    @Query(value = """
            SELECT codigo_nbs AS "codigoNbs",
                   descricao_atividade AS "descricaoAtividade",
                   similarity(descricao_atividade, :termo) AS "score"
            FROM cat_nbs
            ORDER BY descricao_atividade <-> :termo
            LIMIT 5
            """, nativeQuery = true)
    List<SugestaoNbs> buscarSugestoesFuzzy(@Param("termo") String termo);
}
