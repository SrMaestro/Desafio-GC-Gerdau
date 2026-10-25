-- Data da última decisão do comprador na linha (corrigir, decidir parecida, decidir em bloco, descartar).
-- Nula nas linhas que o comprador ainda não tocou e nas gravadas antes desta migration.
ALTER TABLE linhas_importacao ADD COLUMN conferido_em TIMESTAMP;
