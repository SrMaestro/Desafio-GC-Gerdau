package com.Gerdau.Desafio_GC_Gerdau_App.dto;

/**
 * Indicadores do registro de consultas de cadastro; {@code null} significa contagem indisponível.
 * {@code tentativas} é o total de tentativas registradas, {@code barradas} as bloqueadas como duplicadas,
 * {@code reutilizadas} as que terminaram em "Usar este código" e {@code semCorrespondencia} os cadastros novos
 * feitos sem nenhum serviço parecido.
 */
public record Indicadores(Long tentativas, Long barradas, Long reutilizadas, Long semCorrespondencia) {

    /** Parte das tentativas que não gerou código novo (barradas mais reutilizadas), em %; {@code null} sem dados. */
    public Integer percentualEvitado() {
        if (tentativas == null || barradas == null || reutilizadas == null || tentativas == 0) {
            return null;
        }
        return (int) Math.round(100.0 * (barradas + reutilizadas) / tentativas);
    }
}
