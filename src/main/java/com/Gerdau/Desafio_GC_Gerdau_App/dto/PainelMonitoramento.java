package com.Gerdau.Desafio_GC_Gerdau_App.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Indicadores de qualidade da base. Campos {@code null} significam indisponível.
 * {@code percentualAutomatico}: linhas de importação resolvidas sem decisão humana (existentes ou novas) entre as
 * classificadas sem erro. {@code horasMediaAprovacao}: média entre o envio e a decisão do aprovador.
 */
public record PainelMonitoramento(
        long duplicatasPossiveis, List<DuplicataPossivel> duplicatas,
        long servicosSemNbs, long filaAprovacao, long deParaPendentes,
        Integer percentualAutomatico, Double horasMediaAprovacao,
        LocalDateTime ultimaExecucao, List<String> alertas) {
}
