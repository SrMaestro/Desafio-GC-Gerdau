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

    /**
     * Similaridade entre o texto digitado e a descrição oficial, ignorando o texto padrão das descrições
     * ("Serviços de", "de", "e", "não classificados em subposições anteriores"...). Sem isso, essas palavras
     * comuns pesam mais que o termo que importa (ex.: "alvenaria", "andaime").
     */
    @Query(value = """
            WITH p AS (
                SELECT CAST('\\m(servi.os?|de|da|do|das|dos|e|para|em|ou|a|o|com|sem|n.o|classificados?|anteriores|subposi..es|itens?|exceto|outros?)\\M' AS text) AS re
            ),
            t AS (
                SELECT regexp_replace(lower(:texto), p.re, ' ', 'g') AS q, p.re FROM p
            )
            SELECT * FROM (
                SELECT c.codigo_nbs AS codigoNbs, c.descricao_atividade AS descricao,
                       similarity(regexp_replace(lower(c.descricao_atividade), t.re, ' ', 'g'), t.q) AS score
                FROM cat_nbs c, t
            ) r
            WHERE r.score >= :minimo
            ORDER BY r.score DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<NbsSimilar> buscarSugestoes(@Param("texto") String texto,
                                     @Param("minimo") double minimo,
                                     @Param("limite") int limite);
}
