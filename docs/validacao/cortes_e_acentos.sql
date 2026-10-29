-- Complemento da validação: (1) quantos casos passam em cada corte de similaridade e (2) o efeito, só medido,
-- de ignorar acentos antes de comparar. Só leitura. Usa os mesmos casos de casos_similaridade.sql.
WITH lista(grupo, texto, esperado) AS (VALUES
  ('A','Concretagem de fundação',1),('A','concretagem de fundacao',1),('A','Concretagem de fundações',1),
  ('A','Manutenção de motor elétrico',2),('A','manutencao motor eletrico',2),('A','Manutenção do motor elétrico',2),
  ('A','Instalação de painel eletrico',3),('A','Montagem de tubulacao industrial',7),
  ('A','Calandragem de chapas de aço carbono',21),('A','Calibração de instrumento de campo',98),('A','Solda TIG aço inox',6),
  ('B','Manutenção de motor',NULL),('B','Manutenção de bomba de óleo',NULL),('B','Instalação de sensor de temperatura',NULL),
  ('B','Instalação de painel solar',NULL),('B','Solda TIG em aço carbono',NULL),('B','Pintura epóxi residencial',NULL),
  ('B','Corte a plasma de tubos',NULL),
  ('C','Soldagem TIG de inox',6),('C','Aço inox solda TIG',6),('C','CLP programação',5),
  ('C','Programação de controlador lógico',5),('C','Montar andaime',11),('C','Mobilizar canteiro',15),
  ('C','Eletricista industrial',17)
), sem_acento(f) AS (SELECT 1),
medidas AS (
  SELECT c.grupo, c.texto, c.esperado,
         -- nota do serviço esperado (grupos A e C) ou do mais parecido (grupo B)
         CASE WHEN c.esperado IS NOT NULL
              THEN similarity(lower(t.texto_breve), lower(c.texto))
              ELSE (SELECT max(similarity(lower(texto_breve), lower(c.texto))) FROM servicos_mock) END AS atual,
         CASE WHEN c.esperado IS NOT NULL
              THEN similarity(translate(lower(t.texto_breve),'áàâãäéèêëíìîïóòôõöúùûüç','aaaaaeeeeiiiiooooouuuuc'),
                              translate(lower(c.texto),'áàâãäéèêëíìîïóòôõöúùûüç','aaaaaeeeeiiiiooooouuuuc'))
              ELSE (SELECT max(similarity(translate(lower(texto_breve),'áàâãäéèêëíìîïóòôõöúùûüç','aaaaaeeeeiiiiooooouuuuc'),
                                          translate(lower(c.texto),'áàâãäéèêëíìîïóòôõöúùûüç','aaaaaeeeeiiiiooooouuuuc')))
                   FROM servicos_mock) END AS sem_acento
  FROM lista c LEFT JOIN servicos_mock t ON t.id_servico = c.esperado
)
SELECT corte,
       count(*) FILTER (WHERE esperado IS NOT NULL AND atual >= corte)       AS equivalentes_acima,
       count(*) FILTER (WHERE esperado IS NOT NULL)                          AS equivalentes_total,
       count(*) FILTER (WHERE esperado IS NULL AND atual >= corte)           AS diferentes_acima,
       count(*) FILTER (WHERE esperado IS NULL)                              AS diferentes_total,
       count(*) FILTER (WHERE esperado IS NOT NULL AND sem_acento >= corte)  AS equiv_acima_sem_acento,
       count(*) FILTER (WHERE esperado IS NULL AND sem_acento >= corte)      AS dif_acima_sem_acento
FROM medidas, (VALUES (0.4),(0.5),(0.6),(0.7),(0.8)) AS cortes(corte)
GROUP BY corte ORDER BY corte;
