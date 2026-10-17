package com.Gerdau.Desafio_GC_Gerdau_App.repository;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DicionarioDeParaRepository extends JpaRepository<DicionarioDePara, Long> {

    /** Busca exata: só um mapeamento APROVADO vale para o nome (garantido por uq_de_para_aprovado). */
    Optional<DicionarioDePara> findByNomeNormalizadoAndStatus(String nomeNormalizado, StatusDePara status);

    List<DicionarioDePara> findByNomeNormalizadoAndStatusOrderByConfirmacoesDesc(String nomeNormalizado, StatusDePara status);

    /** Fila de revisão: mais confirmados primeiro, os mais antigos desempatam. */
    List<DicionarioDePara> findByStatusOrderByConfirmacoesDescCriadoEmAsc(StatusDePara status);

    /**
     * Registra uma confirmação do usuário. Insere como PENDENTE ou, se o par nome/NBS já está pendente,
     * soma uma confirmação. Não altera mapeamentos já aprovados, rejeitados ou revogados (retorna 0).
     */
    @Modifying
    @Query(value = """
            INSERT INTO dicionario_de_para (nome_normalizado, nome_original, codigo_nbs, origem, score)
            VALUES (:nomeNormalizado, :nomeOriginal, :codigoNbs, 'USUARIO', :score)
            ON CONFLICT (nome_normalizado, codigo_nbs) DO UPDATE
                SET confirmacoes = dicionario_de_para.confirmacoes + 1
                WHERE dicionario_de_para.status = 'PENDENTE'
            """, nativeQuery = true)
    int registrarConfirmacao(@Param("nomeNormalizado") String nomeNormalizado,
                             @Param("nomeOriginal") String nomeOriginal,
                             @Param("codigoNbs") String codigoNbs,
                             @Param("score") Float score);
}
