-- cat_horarios passa a ser a unica fonte das condicoes (antes espelhada no enum Condicao e no CHECK da V1).
-- Condicao nova = INSERT em cat_horarios. Condicao aposentada = ativo = FALSE: some do formulario e da
-- Smart Key, mas servicos ja cadastrados guardam o texto da condicao e continuam como estao.
ALTER TABLE cat_horarios ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE servicos_mock DROP CONSTRAINT servicos_mock_id1_condicao_check;
