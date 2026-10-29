-- Registro de cada tentativa de cadastro individual: o que foi consultado, o que o sistema sugeriu e o que o usuário decidiu.
CREATE TABLE consultas_cadastro (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario       VARCHAR(100) NOT NULL,
    texto_breve   VARCHAR(255) NOT NULL,
    condicao      VARCHAR(50),
    cnae          VARCHAR(15),
    codigo_gerado VARCHAR(40),
    resultado     VARCHAR(20)  NOT NULL,
    decisao       VARCHAR(25),
    servico_id    INTEGER REFERENCES servicos_mock (id_servico),

    CONSTRAINT consultas_cadastro_resultado_check CHECK (resultado IN
        ('BLOQUEADO', 'REQUER_CONFIRMACAO', 'CADASTRADO', 'SUGERIDO')),
    CONSTRAINT consultas_cadastro_decisao_check CHECK (decisao IS NULL OR decisao IN
        ('CADASTROU_NOVO', 'CONFIRMOU_MESMO_ASSIM', 'REUTILIZOU_EXISTENTE'))
);

CREATE INDEX idx_consultas_cadastro_criado_em ON consultas_cadastro (criado_em DESC);

CREATE TABLE consulta_cadastro_sugestoes (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    consulta_id        BIGINT           NOT NULL REFERENCES consultas_cadastro (id),
    servico_id         INTEGER          NOT NULL,
    codigo_padronizado VARCHAR(40)      NOT NULL,
    texto_breve        VARCHAR(255)     NOT NULL,
    score              DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_consulta_cadastro_sugestoes_consulta ON consulta_cadastro_sugestoes (consulta_id);
