package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.DicionarioDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.OrigemDePara;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusDePara;

import java.time.LocalDateTime;

public record DeParaResponse(
        Long id,
        String nomeOriginal,
        String nomeNormalizado,
        String codigoNbs,
        OrigemDePara origem,
        Float score,
        StatusDePara status,
        Integer confirmacoes,
        LocalDateTime criadoEm,
        String revisadoPor,
        LocalDateTime revisadoEm,
        String motivo) {

    public static DeParaResponse de(DicionarioDePara m) {
        return new DeParaResponse(m.getId(), m.getNomeOriginal(), m.getNomeNormalizado(), m.getCodigoNbs(),
                m.getOrigem(), m.getScore(), m.getStatus(), m.getConfirmacoes(), m.getCriadoEm(),
                m.getRevisadoPor(), m.getRevisadoEm(), m.getMotivo());
    }
}
