package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.Servico;
import com.Gerdau.Desafio_GC_Gerdau_App.dto.ServicoSimilar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Texto em que a lista procura: o mesmo que a tabela mostra (id, área, texto, código, CNAE e criador), sem
     * maiúsculas nem acentos. O {@code translate} dispensa a extensão {@code unaccent}.
     */
    String TEXTO_PESQUISAVEL = "translate(lower(concat_ws(' ', s.id_servico, s.area_servico, s.texto_breve,"
            + " s.codigo_padronizado, s.id2_cnae, s.criador_codigo)),"
            + " 'áàâãäéèêëíìîïóòôõöúùûüçñ', 'aaaaaeeeeiiiiooooouuuucn')";

    String FILTRO_PESQUISA = "WHERE (:area = '' OR s.area_servico = :area)"
            + " AND (:termo = '' OR position(:termo IN " + TEXTO_PESQUISAVEL + ") > 0)";

    /**
     * A lista paginada. {@code termo} já vem minúsculo e sem acentos ({@code ""} = sem busca) e é procurado como texto
     * literal, então {@code %} e {@code _} não funcionam como curinga. {@code area} {@code ""} = todas.
     */
    @Query(value = "SELECT s.* FROM servicos_mock s " + FILTRO_PESQUISA + " ORDER BY s.id_servico",
            countQuery = "SELECT count(*) FROM servicos_mock s " + FILTRO_PESQUISA,
            nativeQuery = true)
    Page<Servico> pesquisar(@Param("termo") String termo, @Param("area") String area, Pageable pageable);

    @Query("SELECT DISTINCT s.areaServico FROM Servico s ORDER BY s.areaServico")
    List<String> findAreas();

    List<Servico> findByCriadorCodigo(String criadorCodigo);

    List<Servico> findByAbreviatura(String abreviatura);

    /**
     * O operador {@code %} (limiar padrão do pg_trgm, 0,3, abaixo dos 0,4 usados pelo sistema) deixa o Postgres usar o
     * índice GIN da V3 para descartar o que não se parece; {@code similarity(...) >= :minimo} sozinho obriga a varrer a
     * tabela inteira a cada consulta (medido: ~0,3 s por consulta com 20 mil serviços). O resultado é o mesmo.
     */
    @Query(value = """
            SELECT id_servico AS id, texto_breve AS textoBreve, codigo_padronizado AS codigoPadronizado,
                   id1_condicao AS id1Condicao, id2_cnae AS id2Cnae,
                   similarity(lower(texto_breve), lower(:texto)) AS score
            FROM servicos_mock
            -- % corta pelo limiar do pg_trgm (0,3): :minimo tem que ficar acima dele (LimiarSimilaridadeTest)
            WHERE lower(texto_breve) % lower(:texto)
              AND similarity(lower(texto_breve), lower(:texto)) >= :minimo
            ORDER BY score DESC
            LIMIT :limite
            """, nativeQuery = true)
    List<ServicoSimilar> buscarSimilares(@Param("texto") String texto,
                                         @Param("minimo") double minimo,
                                         @Param("limite") int limite);

    /**
     * O serviço mais parecido que conta como duplicado: mesma condição e mesmo CNAE e similaridade a partir de
     * {@code minimo}. Filtra tudo no banco de propósito: filtrar só os N mais parecidos (como {@code buscarSimilares})
     * perde o duplicado quando há N ou mais serviços com o mesmo texto em outras condições ou CNAEs.
     */
    @Query(value = """
            SELECT id_servico AS id, texto_breve AS textoBreve, codigo_padronizado AS codigoPadronizado,
                   id1_condicao AS id1Condicao, id2_cnae AS id2Cnae,
                   similarity(lower(texto_breve), lower(:texto)) AS score
            FROM servicos_mock
            WHERE lower(texto_breve) % lower(:texto)
              AND similarity(lower(texto_breve), lower(:texto)) >= :minimo
              AND id1_condicao = :condicao
              AND id2_cnae = :cnae
            ORDER BY score DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<ServicoSimilar> buscarDuplicado(@Param("texto") String texto,
                                             @Param("minimo") double minimo,
                                             @Param("condicao") String condicao,
                                             @Param("cnae") String cnae);
}
