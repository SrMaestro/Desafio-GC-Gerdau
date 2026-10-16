CREATE TABLE servicos_mock (
    id_servico         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    area_servico       VARCHAR(50)  NOT NULL,
    texto_breve        VARCHAR(255) NOT NULL,
    abreviatura        VARCHAR(10)  NOT NULL,
    complemento        VARCHAR(15)  DEFAULT '',
    id1_condicao       VARCHAR(50)  NOT NULL,
    id2_cnae           VARCHAR(15)  NOT NULL,
    codigo_padronizado VARCHAR(40)  NOT NULL,
    criador_codigo     VARCHAR(100),
    data_criacao       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT servicos_mock_codigo_padronizado_key UNIQUE (codigo_padronizado),
    CONSTRAINT servicos_mock_id1_condicao_check CHECK (id1_condicao IN (
        'Hora normal',
        'Hora extra Seg a Sab',
        'Hora extra Dom/Feriado',
        'Adicional noturno (hora)',
        'Hora extra noturna Seg a Sab',
        'Hora extra noturna Dom/Feriado',
        'Adicional periculosidade (hora)',
        'Adicional periculosidade s/ hora extra',
        'Adicional periculosidade (mensal)',
        'Adicional insalubridade (hora)',
        'Posto mensal (equipe fixa)',
        'Mobilizacao / Desmobilizacao',
        'Retroativo',
        'Obs.: Itens que não são mão de obra'
    ))
);
