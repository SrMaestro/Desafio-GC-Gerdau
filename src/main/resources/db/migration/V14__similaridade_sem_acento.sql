-- Comparação de texto sem maiúsculas nem acentos. O translate dispensa a extensão unaccent
-- (a lista de serviços já usa a mesma técnica) e a função é IMMUTABLE para poder ser indexada.
CREATE OR REPLACE FUNCTION normaliza_texto(texto text) RETURNS text
    LANGUAGE sql IMMUTABLE PARALLEL SAFE
    AS $$ SELECT translate(lower(texto), 'áàâãäéèêëíìîïóòôõöúùûüçñ', 'aaaaaeeeeiiiiooooouuuucn') $$;

CREATE INDEX idx_servicos_mock_texto_normalizado_trgm
    ON servicos_mock USING gin (normaliza_texto(texto_breve) gin_trgm_ops);

DROP INDEX idx_servicos_mock_texto_breve_trgm;
