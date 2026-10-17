-- Layer 2: modificadores de horário/condição. O sufixo compõe a Smart Key (NBS + sufixo).
-- Valores e sufixos espelham o enum Condicao e o CHECK de id1_condicao da V1.
CREATE TABLE cat_horarios (
    id_horario        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao_horario VARCHAR(100) NOT NULL,
    sufixo_codigo     VARCHAR(10)  NOT NULL,

    CONSTRAINT cat_horarios_descricao_key UNIQUE (descricao_horario),
    CONSTRAINT cat_horarios_sufixo_key UNIQUE (sufixo_codigo)
);

INSERT INTO cat_horarios (descricao_horario, sufixo_codigo)
VALUES
    ('Hora normal',                            'HN'),
    ('Hora extra Seg a Sab',                   'HES'),
    ('Hora extra Dom/Feriado',                 'HED'),
    ('Adicional noturno (hora)',               'NOT'),
    ('Hora extra noturna Seg a Sab',           'HENS'),
    ('Hora extra noturna Dom/Feriado',         'HEND'),
    ('Adicional periculosidade (hora)',        'PERIC'),
    ('Adicional periculosidade s/ hora extra', 'PERS'),
    ('Adicional periculosidade (mensal)',      'PERM'),
    ('Adicional insalubridade (hora)',         'INSAL'),
    ('Posto mensal (equipe fixa)',             'POSTO'),
    ('Mobilizacao / Desmobilizacao',           'MOB'),
    ('Retroativo',                             'RETRO'),
    ('Obs.: Itens que não são mão de obra',    'OBS');
