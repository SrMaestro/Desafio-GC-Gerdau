package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import com.Gerdau.Desafio_GC_Gerdau_App.model.EtapaLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.SituacaoLinha;
import com.Gerdau.Desafio_GC_Gerdau_App.model.StatusLote;

import java.util.Map;

/** Andamento e contagens de um lote de importação. */
public record ResumoLote(
        Long id,
        String arquivo,
        StatusLote status,
        int total,
        int processadas,
        Map<SituacaoLinha, Long> porSituacao,
        Map<EtapaLinha, Long> porEtapa) {
}
