-- Texto normalizado gerado pelo banco (nunca fica fora de sincronia com texto_breve) e unicidade por
-- texto + condição + CNAE, a mesma chave que o cadastro usa para bloquear duplicado.
ALTER TABLE servicos_mock
    ADD COLUMN texto_normalizado TEXT GENERATED ALWAYS AS (normaliza_texto(texto_breve)) STORED;

-- Duplicatas que já existiam: todas menos a mais antiga de cada grupo ficam marcadas como legado, para a unicidade
-- valer daqui em diante sem apagar nem alterar nenhum serviço. O relatório de duplicatas (/ui/monitoramento) lista essas.
ALTER TABLE servicos_mock ADD COLUMN duplicata_legada BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE servicos_mock s
   SET duplicata_legada = TRUE
 WHERE EXISTS (SELECT 1 FROM servicos_mock o
                WHERE o.texto_normalizado = s.texto_normalizado
                  AND o.id1_condicao = s.id1_condicao
                  AND o.id2_cnae = s.id2_cnae
                  AND o.id_servico < s.id_servico);

CREATE UNIQUE INDEX uq_servicos_texto_condicao_cnae
    ON servicos_mock (texto_normalizado, id1_condicao, id2_cnae)
    WHERE NOT duplicata_legada;
