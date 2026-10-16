INSERT INTO servicos_mock
    (area_servico, texto_breve, abreviatura, complemento, id1_condicao, id2_cnae, codigo_padronizado, criador_codigo)
VALUES
    ('Civil',          'Concretagem de fundação',            'CONC',   '_CONCRE', 'Hora normal',                            '4399103', 'CONC_CONCRE_HN4399103',      'bruno.fernandes'),
    ('Eletromecânica', 'Manutenção de motor elétrico',       'MANUT',  '_MOTORE', 'Hora extra Seg a Sab',                   '3313000', 'MANUT_MOTORE_HES3313000',    'leonardo.longaray'),
    ('Eletromecânica', 'Instalação de painel elétrico',      'INSTAL', '_PAINEL', 'Adicional periculosidade (hora)',        '4321400', 'INSTAL_PAINEL_PERIC4321400', NULL),
    ('Civil',          'Construção de alvenaria estrutural', 'CONST',  '_ALVENA', 'Hora extra Dom/Feriado',                 '4120400', 'CONST_ALVENA_HED4120400',    'bruno.fernandes'),
    ('Automação',      'Programação de CLP industrial',      'PROG',   '_CLPIND', 'Hora normal',                            '3321000', 'PROG_CLPIND_HN3321000',      'leonardo.longaray'),
    ('Caldeiraria',    'Solda TIG em aço inox',              'SOLDA',  '_TIGINO', 'Adicional insalubridade (hora)',         '2539001', 'SOLDA_TIGINO_INSAL2539001',  'bruno.fernandes'),
    ('Tubulação',      'Montagem de tubulação industrial',   'MONT',   '_TUBULA', 'Hora extra noturna Seg a Sab',           '3329599', 'MONT_TUBULA_HENS3329599',    NULL),
    ('Eletromecânica', 'Manutenção de bomba centrífuga',     'MANUT',  '_BOMBAS', 'Adicional noturno (hora)',               '3314801', 'MANUT_BOMBAS_NOT3314801',    'leonardo.longaray'),
    ('Civil',          'Pintura epóxi industrial',           'PINT',   '_EPOXIP', 'Adicional periculosidade s/ hora extra', '4330404', 'PINT_EPOXIP_PERS4330404',    'bruno.fernandes'),
    ('Caldeiraria',    'Corte a plasma de chapas',           'CORTE',  '_PLASMA', 'Hora extra Seg a Sab',                   '2532301', 'CORTE_PLASMA_HES2532301',    NULL),
    ('Civil',          'Locação de andaime tubular',         'LOCAC',  '_ANDAIM', 'Obs.: Itens que não são mão de obra',    '7732201', 'LOCAC_ANDAIM_OBS7732201',    'bruno.fernandes'),
    ('Eletromecânica', 'Manutenção de transformador',        'MANUT',  '_TRANSF', 'Adicional periculosidade (mensal)',      '3313000', 'MANUT_TRANSF_PERM3313000',   'leonardo.longaray'),
    ('Automação',      'Instalação de sensor de pressão',    'INSTAL', '_SENSOR', 'Hora normal',                            '3321000', 'INSTAL_SENSOR_HN3321000',    'bruno.fernandes'),
    ('Tubulação',      'Isolamento térmico de dutos',        'ISOLA',  '_TERMIC', 'Adicional insalubridade (hora)',         '4329104', 'ISOLA_TERMIC_INSAL4329104',  NULL),
    ('Civil',          'Mobilização de canteiro de obras',   'MOBIL',  '_CANTEI', 'Mobilizacao / Desmobilizacao',           '4399199', 'MOBIL_CANTEI_MOB4399199',    'bruno.fernandes');
