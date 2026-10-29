-- O comprador pode contestar o bloqueio por texto parecido dizendo por que é outro serviço; a justificativa fica no histórico.
ALTER TABLE consultas_cadastro ADD COLUMN justificativa VARCHAR(500);

ALTER TABLE consultas_cadastro DROP CONSTRAINT consultas_cadastro_decisao_check;
ALTER TABLE consultas_cadastro ADD CONSTRAINT consultas_cadastro_decisao_check CHECK (decisao IS NULL OR decisao IN
    ('CADASTROU_NOVO', 'CONFIRMOU_MESMO_ASSIM', 'REUTILIZOU_EXISTENTE', 'CONTESTOU_BLOQUEIO'));
