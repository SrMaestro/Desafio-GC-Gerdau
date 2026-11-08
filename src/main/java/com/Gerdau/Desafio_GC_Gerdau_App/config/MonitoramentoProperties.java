package com.Gerdau.Desafio_GC_Gerdau_App.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Limites que disparam alerta no painel e no log da rotina semanal (application.properties, prefixo monitoramento). */
@ConfigurationProperties(prefix = "monitoramento")
public record MonitoramentoProperties(Integer limiteDuplicatas, Integer limiteFilaAprovacao, Integer limiteHorasAprovacao) {

    public MonitoramentoProperties {
        limiteDuplicatas = limiteDuplicatas != null ? limiteDuplicatas : 10;
        limiteFilaAprovacao = limiteFilaAprovacao != null ? limiteFilaAprovacao : 50;
        limiteHorasAprovacao = limiteHorasAprovacao != null ? limiteHorasAprovacao : 48;
    }
}
