package com.Gerdau.Desafio_GC_Gerdau_App.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String IMPORTACAO_EXECUTOR = "importacaoExecutor";

    /** Uma importação por vez em segundo plano; as demais esperam na fila. */
    @Bean(name = IMPORTACAO_EXECUTOR)
    public Executor importacaoExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("importacao-");
        executor.initialize();
        return executor;
    }
}
