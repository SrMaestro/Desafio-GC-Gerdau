package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    Optional<Servico> findByCodigoPadronizado(String codigoPadronizado);

    List<Servico> findByAreaServico(String areaServico);

    @Query("SELECT DISTINCT s.areaServico FROM Servico s ORDER BY s.areaServico")
    List<String> findAreas();

    List<Servico> findByCriadorCodigo(String criadorCodigo);

    List<Servico> findByAbreviatura(String abreviatura);

    @Query(value = """
            SELECT id_servico AS id, texto_breve AS textoBreve, codigo_padronizado AS codigoPadronizado,
                   id1_condicao AS id1Condicao, id2_cnae AS id2Cnae,
                   similarity(lower(texto_breve), lower(:texto)) AS score
            FROM servicos_mock
            WHERE similarity(lower(texto_breve), lower(:texto)) >= :minimo
            ORDER BY score DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<ServicoSimilar> buscarSimilares(@Param("texto") String texto,
                                         @Param("minimo") double minimo,
                                         @Param("limite") int limite);
}
