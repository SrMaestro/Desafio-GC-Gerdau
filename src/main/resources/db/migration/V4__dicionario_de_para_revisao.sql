-- Layer 1: catálogo NBS (base oficial, não é alterada pelo aprendizado)
CREATE TABLE cat_nbs (
    codigo_nbs          VARCHAR(20)  PRIMARY KEY,
    descricao_atividade VARCHAR(255) NOT NULL
);

CREATE INDEX idx_cat_nbs_descricao_trgm
    ON cat_nbs USING gin (lower(descricao_atividade) gin_trgm_ops);

-- Layer 3: dicionário De-Para. Só mapeamentos APROVADO valem como busca exata.
CREATE TABLE dicionario_de_para (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_normalizado VARCHAR(255) NOT NULL,
    nome_original    VARCHAR(255) NOT NULL,
    codigo_nbs       VARCHAR(20)  NOT NULL REFERENCES cat_nbs (codigo_nbs),
    origem           VARCHAR(10)  NOT NULL,
    score            REAL,
    status           VARCHAR(10)  NOT NULL DEFAULT 'PENDENTE',
    confirmacoes     INTEGER      NOT NULL DEFAULT 1,
    criado_em        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revisado_por     VARCHAR(100),
    revisado_em      TIMESTAMP,
    motivo           VARCHAR(255),

    CONSTRAINT dicionario_de_para_origem_check
        CHECK (origem IN ('LOTE', 'USUARIO', 'REVISOR')),
    CONSTRAINT dicionario_de_para_status_check
        CHECK (status IN ('PENDENTE', 'APROVADO', 'REJEITADO', 'REVOGADO')),
    CONSTRAINT dicionario_de_para_confirmacoes_check
        CHECK (confirmacoes >= 1),
    CONSTRAINT dicionario_de_para_nome_codigo_key
        UNIQUE (nome_normalizado, codigo_nbs)
);

-- no máximo um mapeamento aprovado por nome
CREATE UNIQUE INDEX uq_de_para_aprovado
    ON dicionario_de_para (nome_normalizado)
    WHERE status = 'APROVADO';

-- fila de revisão: pendentes mais confirmados primeiro
CREATE INDEX idx_de_para_fila
    ON dicionario_de_para (confirmacoes DESC, criado_em)
    WHERE status = 'PENDENTE';

-- trilha de auditoria das mudanças de status
CREATE TABLE dicionario_de_para_historico (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    de_para_id  BIGINT      NOT NULL REFERENCES dicionario_de_para (id),
    de_status   VARCHAR(10),
    para_status VARCHAR(10) NOT NULL,
    usuario     VARCHAR(100),
    motivo      VARCHAR(255),
    em          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_de_para_historico_de_para
    ON dicionario_de_para_historico (de_para_id);
