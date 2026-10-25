-- Importação em massa de serviços: lotes, linhas e sinônimos aprovados.
CREATE TABLE lotes_importacao (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    arquivo_nome  VARCHAR(255) NOT NULL,
    enviado_por   VARCHAR(100) NOT NULL,
    criado_em     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(15)  NOT NULL,
    total_linhas  INTEGER      NOT NULL DEFAULT 0,
    processadas   INTEGER      NOT NULL DEFAULT 0,
    mensagem_erro VARCHAR(500),

    CONSTRAINT lotes_importacao_status_check CHECK (status IN ('PROCESSANDO', 'CONCLUIDO', 'FALHOU'))
);

CREATE TABLE linhas_importacao (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lote_id              BIGINT       NOT NULL REFERENCES lotes_importacao (id),
    numero               INTEGER      NOT NULL,
    area_servico         VARCHAR(50),
    texto_breve          VARCHAR(255),
    abreviatura          VARCHAR(10),
    complemento          VARCHAR(15),
    condicao             VARCHAR(50),
    cnae                 VARCHAR(15),
    codigo_nbs           VARCHAR(20),
    criador_codigo       VARCHAR(100),
    codigo_gerado        VARCHAR(40),
    situacao             VARCHAR(15)  NOT NULL,
    etapa                VARCHAR(15)  NOT NULL,
    servico_parecido_id  INTEGER,
    score                DOUBLE PRECISION,
    motivo               VARCHAR(500),
    decisao              VARCHAR(15),
    conferido_por        VARCHAR(100),
    enviado_em           TIMESTAMP,
    decidido_por         VARCHAR(100),
    decidido_em          TIMESTAMP,
    servico_criado_id    INTEGER,

    CONSTRAINT linhas_importacao_situacao_check CHECK (situacao IN ('ERRO', 'EXISTENTE', 'REPETIDA', 'PARECIDA', 'NOVA')),
    CONSTRAINT linhas_importacao_etapa_check CHECK (etapa IN
        ('EM_CONFERENCIA', 'PRONTA', 'ENVIADA', 'APROVADA', 'REJEITADA', 'CONFLITO', 'IGNORADA', 'DESCARTADA')),
    CONSTRAINT linhas_importacao_decisao_check CHECK (decisao IS NULL OR decisao IN ('MESMO_SERVICO', 'OUTRO_SERVICO'))
);

CREATE INDEX idx_linhas_importacao_lote ON linhas_importacao (lote_id);
CREATE INDEX idx_linhas_importacao_codigo_gerado ON linhas_importacao (codigo_gerado);

CREATE TABLE servico_sinonimo (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    texto_normalizado VARCHAR(255) NOT NULL,
    condicao         VARCHAR(50)  NOT NULL,
    cnae             VARCHAR(15)  NOT NULL,
    servico_id       INTEGER      NOT NULL REFERENCES servicos_mock (id_servico),
    texto_original   VARCHAR(255) NOT NULL,
    aprovado_por     VARCHAR(100) NOT NULL,
    aprovado_em      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    linha_id         BIGINT,

    CONSTRAINT servico_sinonimo_chave_key UNIQUE (texto_normalizado, condicao, cnae)
);
