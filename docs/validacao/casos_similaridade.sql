-- Validação da similaridade: mede, com a mesma fórmula do sistema, cada texto de teste contra o catálogo.
-- Só leitura. Uso: psql -U <usuario> -d <banco> -f docs/validacao/casos_similaridade.sql
-- Colunas de "casos": grupo, texto digitado, id do serviço que a pessoa realmente quis dizer (NULL = nenhum existente).
WITH lista(grupo, texto, esperado) AS (VALUES
  -- A. mesmo serviço escrito de outro jeito: o sistema deveria achar
  ('A', 'Concretagem de fundação',                1),
  ('A', 'concretagem de fundacao',                1),
  ('A', 'Concretagem de fundações',               1),
  ('A', 'Manutenção de motor elétrico',           2),
  ('A', 'manutencao motor eletrico',              2),
  ('A', 'Manutenção do motor elétrico',           2),
  ('A', 'Instalação de painel eletrico',          3),
  ('A', 'Montagem de tubulacao industrial',       7),
  ('A', 'Calandragem de chapas de aço carbono',  21),
  ('A', 'Calibração de instrumento de campo',    98),
  ('A', 'Solda TIG aço inox',                     6),
  -- B. serviço diferente com texto parecido: o sistema não pode tratar como equivalente
  ('B', 'Manutenção de motor',                    NULL),
  ('B', 'Manutenção de bomba de óleo',            NULL),
  ('B', 'Instalação de sensor de temperatura',    NULL),
  ('B', 'Instalação de painel solar',             NULL),
  ('B', 'Solda TIG em aço carbono',               NULL),
  ('B', 'Pintura epóxi residencial',              NULL),
  ('B', 'Corte a plasma de tubos',                NULL),
  -- C. mesmo serviço com palavras diferentes (sinônimo, sigla, ordem, texto curto)
  ('C', 'Soldagem TIG de inox',                   6),
  ('C', 'Aço inox solda TIG',                     6),
  ('C', 'CLP programação',                        5),
  ('C', 'Programação de controlador lógico',      5),
  ('C', 'Montar andaime',                        11),
  ('C', 'Mobilizar canteiro',                    15),
  ('C', 'Eletricista industrial',                17)
), casos AS (SELECT *, row_number() OVER () AS n FROM lista)
SELECT c.grupo,
       c.texto,
       c.esperado,
       m.id_servico                                         AS melhor_id,
       left(m.texto_breve, 38)                              AS melhor_servico,
       round(m.s::numeric, 2)                               AS score,
       CASE WHEN m.s >= 0.8 THEN 'bloqueia*'
            WHEN m.s >= 0.4 THEN 'pede confirmação'
            ELSE 'sem correspondência' END                  AS resposta,
       round(similarity(lower(t.texto_breve), lower(c.texto))::numeric, 2) AS score_do_esperado
FROM casos c
LEFT JOIN LATERAL (
    SELECT id_servico, texto_breve, similarity(lower(texto_breve), lower(c.texto)) AS s
    FROM servicos_mock ORDER BY s DESC, id_servico LIMIT 1
) m ON TRUE
LEFT JOIN servicos_mock t ON t.id_servico = c.esperado
ORDER BY c.n;
-- * "bloqueia" só vale se a condição e o CNAE também forem iguais aos do serviço encontrado (pior caso).

-- D. pares de serviços DIFERENTES que só mudam num detalhe curto no fim do nome (não estão no catálogo de exemplo):
--    mostram o risco de falso bloqueio quando a condição e o CNAE também coincidem.
SELECT a, b, round(similarity(lower(a), lower(b))::numeric, 2) AS score,
       CASE WHEN similarity(lower(a), lower(b)) >= 0.8 THEN 'bloquearia*' ELSE 'pede confirmação' END AS resposta
FROM (VALUES
  ('Solda TIG em aço inox 304',            'Solda TIG em aço inox 316'),
  ('Manutenção de bomba centrífuga 10 cv', 'Manutenção de bomba centrífuga 20 cv'),
  ('Locação de andaime tubular',           'Locação de andaime fachadeiro'),
  ('Pintura epóxi industrial interna',     'Pintura epóxi industrial externa')
) v(a, b);
