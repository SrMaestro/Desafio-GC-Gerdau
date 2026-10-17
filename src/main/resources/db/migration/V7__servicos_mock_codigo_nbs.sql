-- Liga o serviço cadastrado ao código NBS escolhido na busca. Nulo nos serviços anteriores.
ALTER TABLE servicos_mock
    ADD COLUMN codigo_nbs VARCHAR(20) REFERENCES cat_nbs (codigo_nbs);
