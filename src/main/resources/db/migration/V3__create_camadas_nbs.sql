CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- LAYER 1: Catálogo NBS (base oficial do governo, somente leitura para a aplicação)
CREATE TABLE cat_nbs (
    codigo_nbs          VARCHAR(20)  PRIMARY KEY,
    descricao_atividade VARCHAR(255) NOT NULL
);

CREATE INDEX idx_nbs_trgm ON cat_nbs USING GIN (descricao_atividade gin_trgm_ops);

-- LAYER 2: Modificadores de turno de trabalho (sufixo da Smart Key)
CREATE TABLE cat_horarios (
    id_horario        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao_horario VARCHAR(100) NOT NULL,
    sufixo_codigo     VARCHAR(10)  NOT NULL
);

-- LAYER 3: Dicionário De-Para (governança / aprendizado contínuo)
CREATE TABLE dicionario_de_para (
    id                 INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_sujo_digitado VARCHAR(255) NOT NULL,
    codigo_nbs_oficial VARCHAR(20)  REFERENCES cat_nbs (codigo_nbs),
    score_confianca    DOUBLE PRECISION,

    CONSTRAINT dicionario_de_para_nome_sujo_key UNIQUE (nome_sujo_digitado)
);
