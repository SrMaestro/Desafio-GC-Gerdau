-- Retrato de cada rodada do monitoramento semanal (MonitoramentoService), para acompanhar a evolução.
CREATE TABLE monitoramento_execucao (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    executado_em        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duplicatas_possiveis BIGINT   NOT NULL,
    servicos_sem_nbs    BIGINT    NOT NULL,
    fila_aprovacao      BIGINT    NOT NULL,
    depara_pendentes    BIGINT    NOT NULL
);
