CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_servicos_mock_texto_breve_trgm
    ON servicos_mock USING gin (lower(texto_breve) gin_trgm_ops);
