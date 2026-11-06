-- Dados de demonstração: um recorte realista do problema descrito no TAP (milhares de códigos criados sem filtro).
-- Acrescenta serviços industriais de siderurgia no padrão do projeto (inclusive quase-duplicados antigos), o histórico de
-- consultas de cadastro, o dicionário De-Para e três lotes de importação em estados diferentes. Não altera nenhuma
-- migration. Só roda com o perfil "demo" (migration repetível: roda uma vez e não muda de checksum; para recarregar,
-- recrie o banco). As datas são relativas ao momento em que a migration roda.

INSERT INTO servicos_mock
    (area_servico, texto_breve, abreviatura, complemento, id1_condicao, id2_cnae, codigo_padronizado, criador_codigo, data_criacao, codigo_nbs)
VALUES
    ('Inspeção e ensaios', 'Analise de vibracao em equipamentos rotativos', 'VIBRAC', '_ROTAT', 'Hora normal', '7120100', 'VIBRAC_ROTAT_HN7120100', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 50745308), '1.1404.43.00'),
    ('Inspeção e ensaios', 'Ensaio liquido penetrante (LP)', 'ENS', '_LIQPE2', 'Hora normal', '7120100', 'ENS_LIQPE2_HN7120100', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 49093291), '1.1404.49.00'),
    ('Eletromecânica', 'Troca eletrodos grafite forno', 'TROCA', '_GRAFIT', 'Hora normal', '3314718', 'TROCA_GRAFIT_HN3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 48300703), '1.2001.50.00'),
    ('Limpeza industrial', 'Limpeza industrial laminadores', 'LIMPIND', '_LAMINA', 'Hora normal', '8129000', 'LIMPIND_LAMINA_HN8129000', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 47793955), '1.1803.29.00'),
    ('Eletromecânica', 'Manutenção de compressores de ar', 'MANUT', '_COMPRS', 'Hora normal', '3314704', 'MANUT_COMPRS_HN3314704', NULL, CURRENT_TIMESTAMP - make_interval(secs => 47287956), '1.2001.50.00'),
    ('Caldeiraria', 'Soldagem reparo aço-carbono', 'SOLD', '_ACOCAR', 'Hora normal', '2539001', 'SOLD_ACOCAR_HN2539001', NULL, CURRENT_TIMESTAMP - make_interval(secs => 45909260), '1.2001.10.00'),
    ('Inspeção e ensaios', 'Análise de óleo lubrificante', 'ANAOLE', '_LUBRIF', 'Hora normal', '7120100', 'ANAOLE_LUBRIF_HN7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 44017238), '1.1404.41.00'),
    ('Inspeção e ensaios', 'Análise de vibração de equipamentos rotativos', 'ANAVIB', '_ROTATI', 'Hora normal', '7120100', 'ANAVIB_ROTATI_HN7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 43919685), '1.1404.43.00'),
    ('Inspeção e ensaios', 'Análise de óleo lubrificante', 'ANAOLE', '_LUBRIF', 'Hora extra Seg a Sab', '7120100', 'ANAOLE_LUBRIF_HES7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 43841206), '1.1404.41.00'),
    ('Automação', 'Manutenção de inversores de frequência', 'MANUT', '_INVERS', 'Hora normal', '3313999', 'MANUT_INVERS_HN3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 43750462), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Análise de vibração de equipamentos rotativos', 'ANAVIB', '_ROTATI', 'Hora extra Seg a Sab', '7120100', 'ANAVIB_ROTATI_HES7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 42945768), '1.1404.43.00'),
    ('Automação', 'Manutenção de inversores de frequência', 'MANUT', '_INVERS', 'Adicional noturno (hora)', '3313999', 'MANUT_INVERS_NOT3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 42877998), '1.2001.50.00'),
    ('Civil', 'Revestimento anticorrosivo de tanques', 'REVEST', '_TANQUE', 'Hora normal', '4330405', 'REVEST_TANQUE_HN4330405', NULL, CURRENT_TIMESTAMP - make_interval(secs => 42856090), '1.0107.30.00'),
    ('Civil', 'Montagem de estrutura metálica de galpão', 'MONT', '_GALPAO', 'Hora normal', '4292801', 'MONT_GALPAO_HN4292801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 42774447), '1.0105.50.00'),
    ('Inspeção e ensaios', 'Análise de óleo lubrificante', 'ANAOLE', '_LUBRIF', 'Hora extra Dom/Feriado', '7120100', 'ANAOLE_LUBRIF_HED7120100', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 42622663), '1.1404.41.00'),
    ('Limpeza industrial', 'Limpeza industrial de laminador', 'LIMP', '_LAMINA', 'Hora normal', '8129000', 'LIMP_LAMINA_HN8129000', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 42177180), '1.1803.29.00'),
    ('Limpeza industrial', 'Limpeza industrial do laminador', 'LIMP', '_LAMIND', 'Hora normal', '8129000', 'LIMP_LAMIND_HN8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 42084089), '1.1803.29.00'),
    ('Inspeção e ensaios', 'Ensaio com líquido penetrante', 'ENSAIO', '_LP', 'Hora normal', '7120100', 'ENSAIO_LP_HN7120100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 41840219), '1.1404.49.00'),
    ('Eletromecânica', 'Manutencao de redutores de velocidade', 'MANUT', '_REDUTR', 'Hora normal', '3314705', 'MANUT_REDUTR_HN3314705', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 41659142), '1.2001.50.00'),
    ('Civil', 'Revestimento anticorrosivo de tanques', 'REVEST', '_TANQUE', 'Hora extra Seg a Sab', '4330405', 'REVEST_TANQUE_HES4330405', NULL, CURRENT_TIMESTAMP - make_interval(secs => 41579880), '1.0107.30.00'),
    ('Civil', 'Revestimento anticorrosivo de tanques', 'REVEST', '_TANQUE', 'Mobilizacao / Desmobilizacao', '4330405', 'REVEST_TANQUE_MOB4330405', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 41134354), '1.0107.30.00'),
    ('Eletromecânica', 'Instalação de iluminação industrial', 'INSTAL', '_ILUMIN', 'Hora normal', '4321500', 'INSTAL_ILUMIN_HN4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40977811), '1.0106.11.00'),
    ('Eletromecânica', 'Instalação de iluminação industrial', 'INSTAL', '_ILUMIN', 'Adicional noturno (hora)', '4321500', 'INSTAL_ILUMIN_NOT4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40959864), '1.0106.11.00'),
    ('Civil', 'Montagem de estrutura metálica de galpão', 'MONT', '_GALPAO', 'Hora extra Dom/Feriado', '4292801', 'MONT_GALPAO_HED4292801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40900962), '1.0105.50.00'),
    ('Automação', 'Manutenção de inversores de frequência', 'MANUT', '_INVERS', 'Hora extra Dom/Feriado', '3313999', 'MANUT_INVERS_HED3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40792226), '1.2001.50.00'),
    ('Segurança', 'Vigia de espaço confinado', 'VIGIAE', '_CONFIN', 'Hora normal', '8011101', 'VIGIAE_CONFIN_HN8011101', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 40702839), '1.1801.29.00'),
    ('Refratários', 'Revestimento refratário de forno de reaquecimento', 'REFRAT', '_FORREA', 'Hora normal', '4330499', 'REFRAT_FORREA_HN4330499', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 40611960), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Análise de vibração de equipamentos rotativos', 'ANAVIB', '_ROTATI', 'Hora extra Dom/Feriado', '7120100', 'ANAVIB_ROTATI_HED7120100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40440624), '1.1404.43.00'),
    ('Limpeza industrial', 'Limpeza industrial de laminador', 'LIMP', '_LAMINA', 'Adicional insalubridade (hora)', '8129000', 'LIMP_LAMINA_INSAL8129000', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 40288835), '1.1803.29.00'),
    ('Segurança', 'Vigia de espaço confinado', 'VIGIAE', '_CONFIN', 'Adicional noturno (hora)', '8011101', 'VIGIAE_CONFIN_NOT8011101', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40033477), '1.1801.29.00'),
    ('Segurança', 'Vigia de espaço confinado', 'VIGIAE', '_CONFIN', 'Hora extra Seg a Sab', '8011101', 'VIGIAE_CONFIN_HES8011101', NULL, CURRENT_TIMESTAMP - make_interval(secs => 40016671), '1.1801.29.00'),
    ('Eletromecânica', 'Lançamento de cabos de força', 'LANCAB', '_FORCA', 'Hora normal', '4321500', 'LANCAB_FORCA_HN4321500', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 39939879), '1.0106.11.00'),
    ('Eletromecânica', 'Lançamento de cabos de força', 'LANCAB', '_FORCA', 'Adicional periculosidade (mensal)', '4321500', 'LANCAB_FORCA_PERM4321500', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 39934301), '1.0106.11.00'),
    ('Eletromecânica', 'Lançamento de cabos de força', 'LANCAB', '_FORCA', 'Hora extra Seg a Sab', '4321500', 'LANCAB_FORCA_HES4321500', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 39856598), '1.0106.11.00'),
    ('Segurança', 'Acompanhamento de segurança do trabalho em parada', 'SEGPAR', '_ACOMP', 'Posto mensal (equipe fixa)', '7119704', 'SEGPAR_ACOMP_POSTO7119704', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 39509114), '1.1403.90.00'),
    ('Inspeção e ensaios', 'Ensaio por partículas magnéticas', 'ENSAIO', '_PARTMA', 'Hora normal', '7120100', 'ENSAIO_PARTMA_HN7120100', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 39342393), '1.1404.49.00'),
    ('Limpeza industrial', 'Limpeza de filtros de mangas', 'LIMP', '_MANGAS', 'Hora normal', '8129000', 'LIMP_MANGAS_HN8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 39260051), '1.1803.29.00'),
    ('Inspeção e ensaios', 'Medição de espessura de chapas', 'MEDESP', '_CHAPAS', 'Hora normal', '7120100', 'MEDESP_CHAPAS_HN7120100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 39080859), '1.1404.42.00'),
    ('Eletromecânica', 'Instalação de iluminação industrial', 'INSTAL', '_ILUMIN', 'Hora extra Seg a Sab', '4321500', 'INSTAL_ILUMIN_HES4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 39065098), '1.0106.11.00'),
    ('Segurança', 'Acompanhamento de segurança do trabalho em parada', 'SEGPAR', '_ACOMP', 'Hora normal', '7119704', 'SEGPAR_ACOMP_HN7119704', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 38969746), '1.1403.90.00'),
    ('Tubulação', 'Aplicação de manta acústica em ventiladores', 'MANTA', '_ACUSTI', 'Hora normal', '4329105', 'MANTA_ACUSTI_HN4329105', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 38896534), '1.0106.50.00'),
    ('Refratários', 'Revestimento refratário de forno de reaquecimento', 'REFRAT', '_FORREA', 'Hora extra Seg a Sab', '4330499', 'REFRAT_FORREA_HES4330499', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 38886967), '1.2001.50.00'),
    ('Tubulação', 'Aplicação de manta acústica em ventiladores', 'MANTA', '_ACUSTI', 'Adicional noturno (hora)', '4329105', 'MANTA_ACUSTI_NOT4329105', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 38645522), '1.0106.50.00'),
    ('Automação', 'Manutenção de inversores de frequência', 'MANUT', '_INVERS', 'Hora extra Seg a Sab', '3313999', 'MANUT_INVERS_HES3313999', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 38541370), '1.2001.50.00'),
    ('Limpeza industrial', 'Limpeza industrial de laminador', 'LIMP', '_LAMINA', 'Adicional noturno (hora)', '8129000', 'LIMP_LAMINA_NOT8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 38382927), '1.1803.29.00'),
    ('Civil', 'Demolição de estrutura de concreto armado', 'DEMOL', '_ESTCON', 'Hora normal', '4311801', 'DEMOL_ESTCON_HN4311801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 38034848), '1.0103.10.00'),
    ('Refratários', 'Manutenção de válvula gaveta de panela', 'MANUT', '_GAVETA', 'Hora normal', '3314703', 'MANUT_GAVETA_HN3314703', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 37937640), '1.2001.50.00'),
    ('Refratários', 'Manutenção de válvula gaveta de panela', 'MANUT', '_GAVETA', 'Adicional noturno (hora)', '3314703', 'MANUT_GAVETA_NOT3314703', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 37851436), '1.2001.50.00'),
    ('Caldeiraria', 'Fabricação de dutos de exaustão', 'FABRIC', '_DUTOEX', 'Hora normal', '2511000', 'FABRIC_DUTOEX_HN2511000', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 37787514), '1.0106.32.00'),
    ('Civil', 'Montagem de estrutura metálica de galpão', 'MONT', '_GALPAO', 'Mobilizacao / Desmobilizacao', '4292801', 'MONT_GALPAO_MOB4292801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 37768796), '1.0105.50.00'),
    ('Limpeza industrial', 'Limpeza de filtros de mangas', 'LIMP', '_MANGAS', 'Adicional insalubridade (hora)', '8129000', 'LIMP_MANGAS_INSAL8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 37607868), '1.1803.29.00'),
    ('Civil', 'Montagem de andaime fachadeiro', 'MONTAND', '_FACHAD', 'Hora normal', '4399102', 'MONTAND_FACHAD_HN4399102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 37350529), '1.0105.70.00'),
    ('Eletromecânica', 'Manutenção de compressor de ar', 'MANUT', '_COMPRE', 'Hora normal', '3314704', 'MANUT_COMPRE_HN3314704', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 37342872), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Medição de espessura de chapas', 'MEDESP', '_CHAPAS', 'Hora extra Dom/Feriado', '7120100', 'MEDESP_CHAPAS_HED7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 37187718), '1.1404.42.00'),
    ('Caldeiraria', 'Fabricação de dutos de exaustão', 'FABRIC', '_DUTOEX', 'Hora extra Seg a Sab', '2511000', 'FABRIC_DUTOEX_HES2511000', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 37185691), '1.0106.32.00'),
    ('Caldeiraria', 'Fabricação de chaparia e calhas', 'FABRIC', '_CHAPAR', 'Hora normal', '2511000', 'FABRIC_CHAPAR_HN2511000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 37180867), '1.2001.10.00'),
    ('Caldeiraria', 'Fabricação de chaparia e calhas', 'FABRIC', '_CHAPAR', 'Hora extra noturna Seg a Sab', '2511000', 'FABRIC_CHAPAR_HENS2511000', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 36993031), '1.2001.10.00'),
    ('Inspeção e ensaios', 'Ensaio por partículas magnéticas', 'ENSAIO', '_PARTMA', 'Hora extra Seg a Sab', '7120100', 'ENSAIO_PARTMA_HES7120100', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 36896910), '1.1404.49.00'),
    ('Inspeção e ensaios', 'Medição de espessura de chapas', 'MEDESP', '_CHAPAS', 'Hora extra Seg a Sab', '7120100', 'MEDESP_CHAPAS_HES7120100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 36813541), '1.1404.42.00'),
    ('Apoio e logística', 'Transporte de materiais com carreta', 'TRANSP', '_CARRET', 'Obs.: Itens que não são mão de obra', '4930202', 'TRANSP_CARRET_OBS4930202', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 36724707), '1.1101.12.00'),
    ('Inspeção e ensaios', 'Ensaio por partículas magnéticas', 'ENSAIO', '_PARTMA', 'Hora extra Dom/Feriado', '7120100', 'ENSAIO_PARTMA_HED7120100', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 36724020), '1.1404.49.00'),
    ('Automação', 'Configuração de redes industriais', 'CONFIG', '_REDES', 'Hora normal', '6209100', 'CONFIG_REDES_HN6209100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 36660726), '1.2001.50.00'),
    ('Apoio e logística', 'Transporte de materiais com carreta', 'TRANSP', '_CARRET', 'Hora normal', '4930202', 'TRANSP_CARRET_HN4930202', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 36642573), '1.1101.12.00'),
    ('Limpeza industrial', 'Limpeza de filtros de mangas', 'LIMP', '_MANGAS', 'Adicional noturno (hora)', '8129000', 'LIMP_MANGAS_NOT8129000', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 36326312), '1.1803.29.00'),
    ('Civil', 'Escavação mecanizada de valas', 'ESCAV', '_VALAS', 'Mobilizacao / Desmobilizacao', '4313400', 'ESCAV_VALAS_MOB4313400', NULL, CURRENT_TIMESTAMP - make_interval(secs => 35962560), '1.0103.30.00'),
    ('Caldeiraria', 'Soldagem de reparo em aço carbono', 'SOLDA', '_CARBON', 'Hora normal', '2539001', 'SOLDA_CARBON_HN2539001', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 35688366), '1.2001.10.00'),
    ('Civil', 'Demolição de estrutura de concreto armado', 'DEMOL', '_ESTCON', 'Mobilizacao / Desmobilizacao', '4311801', 'DEMOL_ESTCON_MOB4311801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 35634642), '1.0103.10.00'),
    ('Caldeiraria', 'Fabricação de dutos de exaustão', 'FABRIC', '_DUTOEX', 'Adicional noturno (hora)', '2511000', 'FABRIC_DUTOEX_NOT2511000', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 35513105), '1.0106.32.00'),
    ('Eletromecânica', 'Manutenção de compressor de ar', 'MANUT', '_COMPRE', 'Hora extra noturna Seg a Sab', '3314704', 'MANUT_COMPRE_HENS3314704', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 35005171), '1.2001.50.00'),
    ('Refratários', 'Manutenção de válvula gaveta de panela', 'MANUT', '_GAVETA', 'Hora extra Seg a Sab', '3314703', 'MANUT_GAVETA_HES3314703', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 34666207), '1.2001.50.00'),
    ('Caldeiraria', 'Soldagem de reparo em aço carbono', 'SOLDA', '_CARBON', 'Adicional noturno (hora)', '2539001', 'SOLDA_CARBON_NOT2539001', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 34496271), '1.2001.10.00'),
    ('Refratários', 'Secagem e aquecimento de revestimento refratário', 'SECAG', '_AQUECI', 'Adicional noturno (hora)', '3314718', 'SECAG_AQUECI_NOT3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 34302128), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de válvulas industriais', 'MANUT', '_VALVUL', 'Hora normal', '3314703', 'MANUT_VALVUL_HN3314703', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 34224773), '1.2001.50.00'),
    ('Automação', 'Configuração de redes industriais', 'CONFIG', '_REDES', 'Hora extra Dom/Feriado', '6209100', 'CONFIG_REDES_HED6209100', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 34163787), '1.2001.50.00'),
    ('Tubulação', 'Aplicação de manta acústica em ventiladores', 'MANTA', '_ACUSTI', 'Hora extra Seg a Sab', '4329105', 'MANTA_ACUSTI_HES4329105', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 33715905), '1.0106.50.00'),
    ('Civil', 'Execução de bases de equipamentos em concreto', 'BASEEQ', '_EQUIPA', 'Hora extra Seg a Sab', '4399103', 'BASEEQ_EQUIPA_HES4399103', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 33449155), '1.0105.40.00'),
    ('Apoio e logística', 'Locação de contêiner almoxarifado', 'LOCAC', '_CONTAI', 'Obs.: Itens que não são mão de obra', '7739099', 'LOCAC_CONTAI_OBS7739099', NULL, CURRENT_TIMESTAMP - make_interval(secs => 33377818), '1.1101.17.00'),
    ('Civil', 'Demolição de estrutura de concreto armado', 'DEMOL', '_ESTCON', 'Hora extra Seg a Sab', '4311801', 'DEMOL_ESTCON_HES4311801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 33374572), '1.0103.10.00'),
    ('Caldeiraria', 'Fabricação de chaparia e calhas', 'FABRIC', '_CHAPAR', 'Hora extra Dom/Feriado', '2511000', 'FABRIC_CHAPAR_HED2511000', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 33009965), '1.2001.10.00'),
    ('Apoio e logística', 'Locação de guindaste hidráulico 50 t', 'LOCAC', '_GUINDA', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_GUINDA_OBS7732201', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32946618), '1.1101.30.00'),
    ('Eletromecânica', 'Manutenção de válvulas industriais', 'MANUT', '_VALVUL', 'Adicional noturno (hora)', '3314703', 'MANUT_VALVUL_NOT3314703', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 32918748), '1.2001.50.00'),
    ('Caldeiraria', 'Fabricação de dutos de exaustão', 'FABRIC', '_DUTOEX', 'Hora extra Dom/Feriado', '2511000', 'FABRIC_DUTOEX_HED2511000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32844077), '1.0106.32.00'),
    ('Limpeza industrial', 'Hidro jateamento de equipamentos', 'HIDRO', '_EQUIP', 'Hora normal', '8129000', 'HIDRO_EQUIP_HN8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32668223), '1.1803.29.00'),
    ('Limpeza industrial', 'Transporte interno de resíduos industriais', 'TRANSP', '_RESIDU', 'Hora normal', '3821100', 'TRANSP_RESIDU_HN3821100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32579253), '1.1803.29.00'),
    ('Limpeza industrial', 'Limpeza de filtros de mangas', 'LIMP', '_MANGAS', 'Hora extra Seg a Sab', '8129000', 'LIMP_MANGAS_HES8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32229109), '1.1803.29.00'),
    ('Caldeiraria', 'Tratamento térmico de alívio de tensões', 'TRATER', '_ALIVIO', 'Hora normal', '2539002', 'TRATER_ALIVIO_HN2539002', NULL, CURRENT_TIMESTAMP - make_interval(secs => 32055471), '1.2001.10.00'),
    ('Eletromecânica', 'Manutenção de gerador de emergência', 'MANUT', '_GERADO', 'Hora normal', '3313901', 'MANUT_GERADO_HN3313901', NULL, CURRENT_TIMESTAMP - make_interval(secs => 31914028), '1.2001.50.00'),
    ('Limpeza industrial', 'Transporte interno de resíduos industriais', 'TRANSP', '_RESIDU', 'Adicional insalubridade (hora)', '3821100', 'TRANSP_RESIDU_INSAL3821100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 31729155), '1.1803.29.00'),
    ('Caldeiraria', 'Fabricação de chaparia e calhas', 'FABRIC', '_CHAPAR', 'Adicional noturno (hora)', '2511000', 'FABRIC_CHAPAR_NOT2511000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 31719159), '1.2001.10.00'),
    ('Eletromecânica', 'Instalação de eletrocalhas e leitos', 'INSTAL', '_CALHAS', 'Hora normal', '4321500', 'INSTAL_CALHAS_HN4321500', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 31653591), '1.0106.11.00'),
    ('Caldeiraria', 'Tratamento térmico de alívio de tensões', 'TRATER', '_ALIVIO', 'Hora extra Dom/Feriado', '2539002', 'TRATER_ALIVIO_HED2539002', NULL, CURRENT_TIMESTAMP - make_interval(secs => 31561213), '1.2001.10.00'),
    ('Eletromecânica', 'Manut compressor ar', 'MCOMP', '_AR', 'Hora normal', '3314704', 'MCOMP_AR_HN3314704', NULL, CURRENT_TIMESTAMP - make_interval(secs => 31209008), '1.2001.50.00'),
    ('Tubulação', 'Teste hidrostático de linhas', 'TESTEH', '_HIDROS', 'Hora normal', '7120100', 'TESTEH_HIDROS_HN7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 31135087), '1.1404.42.00'),
    ('Eletromecânica', 'Instalação de eletrocalhas e leitos', 'INSTAL', '_CALHAS', 'Adicional periculosidade (mensal)', '4321500', 'INSTAL_CALHAS_PERM4321500', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 30759170), '1.0106.11.00'),
    ('Limpeza industrial', 'Limpeza geral de áreas administrativas', 'LIMP', '_ADMIN', 'Posto mensal (equipe fixa)', '8121400', 'LIMP_ADMIN_POSTO8121400', NULL, CURRENT_TIMESTAMP - make_interval(secs => 30179653), '1.1803.10.00'),
    ('Tubulação', 'Instalação de linhas de ar comprimido', 'INSTAL', '_ARCOMP', 'Hora extra Dom/Feriado', '3329599', 'INSTAL_ARCOMP_HED3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 30007899), '1.0106.90.00'),
    ('Tubulação', 'Instalação de linhas de ar comprimido', 'INSTAL', '_ARCOMP', 'Hora normal', '3329599', 'INSTAL_ARCOMP_HN3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 29986531), '1.0106.90.00'),
    ('Automação', 'Calibração instrumentos pressão e temperatura', 'CALIB', '_PRETEM', 'Hora normal', '3312102', 'CALIB_PRETEM_HN3312102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 29929418), '1.1404.49.00'),
    ('Eletromecânica', 'Manutenção de válvulas industriais', 'MANUT', '_VALVUL', 'Hora extra Dom/Feriado', '3314703', 'MANUT_VALVUL_HED3314703', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 29896858), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de gerador de emergência', 'MANUT', '_GERADO', 'Hora extra Dom/Feriado', '3313901', 'MANUT_GERADO_HED3313901', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 29822814), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de gerador de emergência', 'MANUT', '_GERADO', 'Hora extra noturna Seg a Sab', '3313901', 'MANUT_GERADO_HENS3313901', NULL, CURRENT_TIMESTAMP - make_interval(secs => 29668379), '1.2001.50.00'),
    ('Tubulação', 'Instalação de linhas de ar comprimido', 'INSTAL', '_ARCOMP', 'Hora extra noturna Seg a Sab', '3329599', 'INSTAL_ARCOMP_HENS3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 29664110), '1.0106.90.00'),
    ('Caldeiraria', 'Usinagem de peças de reposição', 'USINAG', '_PECASR', 'Hora extra Seg a Sab', '2539001', 'USINAG_PECASR_HES2539001', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 29462778), '1.2001.10.00'),
    ('Eletromecânica', 'Manutenção de gerador de emergência', 'MANUT', '_GERADO', 'Adicional noturno (hora)', '3313901', 'MANUT_GERADO_NOT3313901', NULL, CURRENT_TIMESTAMP - make_interval(secs => 29321921), '1.2001.50.00'),
    ('Tubulação', 'Teste hidrostático de linhas', 'TESTEH', '_HIDROS', 'Hora extra Seg a Sab', '7120100', 'TESTEH_HIDROS_HES7120100', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 29310728), '1.1404.42.00'),
    ('Eletromecânica', 'Balanceamento dinâmico de ventiladores', 'BALAN', '_VENTIL', 'Hora normal', '3314710', 'BALAN_VENTIL_HN3314710', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 29031147), '1.2001.50.00'),
    ('Eletromecânica', 'Troca de eletrodos de grafite do forno', 'TROCA', '_ELETRO', 'Hora normal', '3314718', 'TROCA_ELETRO_HN3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 28868343), '1.2001.50.00'),
    ('Tubulação', 'Instalação de linhas de ar comprimido', 'INSTAL', '_ARCOMP', 'Hora extra Seg a Sab', '3329599', 'INSTAL_ARCOMP_HES3329599', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 28700324), '1.0106.90.00'),
    ('Civil', 'Execução de estaca hélice contínua', 'ESTACA', '_HELICE', 'Hora normal', '4391600', 'ESTACA_HELICE_HN4391600', NULL, CURRENT_TIMESTAMP - make_interval(secs => 28693254), '1.0105.11.00'),
    ('Eletromecânica', 'Troca de eletrodos de grafite do forno', 'TROCA', '_ELETRO', 'Hora extra Dom/Feriado', '3314718', 'TROCA_ELETRO_HED3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 28632150), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção redutor velocidade', 'MAN', '_REDVEL', 'Hora normal', '3314705', 'MAN_REDVEL_HN3314705', NULL, CURRENT_TIMESTAMP - make_interval(secs => 28604483), '1.2001.50.00'),
    ('Tubulação', 'Teste hidrostático de linhas', 'TESTEH', '_HIDROS', 'Hora extra Dom/Feriado', '7120100', 'TESTEH_HIDROS_HED7120100', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 28548052), '1.1404.42.00'),
    ('Tubulação', 'Substituição de trechos de linha de água de refrigeração', 'SUBST', '_AGUARE', 'Hora normal', '4322301', 'SUBST_AGUARE_HN4322301', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 28350270), '1.0106.21.00'),
    ('Civil', 'Recuperação de piso industrial em concreto', 'RECUP', '_PISOIN', 'Hora normal', '4330405', 'RECUP_PISOIN_HN4330405', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 28267433), '1.0107.40.00'),
    ('Tubulação', 'Substituição de trechos de linha de água de refrigeração', 'SUBST', '_AGUARE', 'Hora extra Dom/Feriado', '4322301', 'SUBST_AGUARE_HED4322301', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 28167641), '1.0106.21.00'),
    ('Caldeiraria', 'Tratamento térmico de alívio de tensões', 'TRATER', '_ALIVIO', 'Hora extra Seg a Sab', '2539002', 'TRATER_ALIVIO_HES2539002', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 27743593), '1.2001.10.00'),
    ('Eletromecânica', 'Balanceamento dinâmico de ventiladores', 'BALAN', '_VENTIL', 'Adicional noturno (hora)', '3314710', 'BALAN_VENTIL_NOT3314710', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 27738220), '1.2001.50.00'),
    ('Eletromecânica', 'Instalação de sistema de aterramento', 'INSTAL', '_ATERRA', 'Adicional noturno (hora)', '4321500', 'INSTAL_ATERRA_NOT4321500', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 27679501), '1.0106.19.00'),
    ('Eletromecânica', 'Instalação de eletrocalhas e leitos', 'INSTAL', '_CALHAS', 'Hora extra Seg a Sab', '4321500', 'INSTAL_CALHAS_HES4321500', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 27489609), '1.0106.11.00'),
    ('Civil', 'Execução de estaca hélice contínua', 'ESTACA', '_HELICE', 'Hora extra Dom/Feriado', '4391600', 'ESTACA_HELICE_HED4391600', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 26812141), '1.0105.11.00'),
    ('Automação', 'Calibração de instrumentos de pressão e temperatura', 'CALIB', '_INSTRU', 'Hora normal', '3312102', 'CALIB_INSTRU_HN3312102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 26559309), '1.1404.49.00'),
    ('Civil', 'Impermeabilizacao laje de cobertura', 'IMPER', '_LAJCOB', 'Hora normal', '4330401', 'IMPER_LAJCOB_HN4330401', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 26458515), '1.0105.30.00'),
    ('Eletromecânica', 'Instalação de sistema de aterramento', 'INSTAL', '_ATERRA', 'Adicional periculosidade (hora)', '4321500', 'INSTAL_ATERRA_PERIC4321500', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 26296147), '1.0106.19.00'),
    ('Eletromecânica', 'Instalação de eletrocalhas e leitos', 'INSTAL', '_CALHAS', 'Adicional periculosidade (hora)', '4321500', 'INSTAL_CALHAS_PERIC4321500', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 26196805), '1.0106.11.00'),
    ('Civil', 'Pintura anti-corrosiva de estrutura metálica', 'PINTAC', '_ESTMT', 'Hora normal', '4330404', 'PINTAC_ESTMT_HN4330404', NULL, CURRENT_TIMESTAMP - make_interval(secs => 26192475), '1.0107.30.00'),
    ('Civil', 'Recuperação de piso industrial em concreto', 'RECUP', '_PISOIN', 'Hora extra Dom/Feriado', '4330405', 'RECUP_PISOIN_HED4330405', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 26130323), '1.0107.40.00'),
    ('Civil', 'Execução de estaca hélice contínua', 'ESTACA', '_HELICE', 'Mobilizacao / Desmobilizacao', '4391600', 'ESTACA_HELICE_MOB4391600', NULL, CURRENT_TIMESTAMP - make_interval(secs => 26107802), '1.0105.11.00'),
    ('Automação', 'Calibração de instrumentos de pressão e temperatura', 'CALIB', '_INSTRU', 'Hora extra Seg a Sab', '3312102', 'CALIB_INSTRU_HES3312102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 26024139), '1.1404.49.00'),
    ('Apoio e logística', 'Locação de caminhão munck', 'LOCAC', '_MUNCK', 'Obs.: Itens que não são mão de obra', '7719599', 'LOCAC_MUNCK_OBS7719599', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 26007346), '1.1101.12.00'),
    ('Limpeza industrial', 'Hidrojateamento equipamentos', 'JATEAM', '_EQUIPA', 'Hora normal', '8129000', 'JATEAM_EQUIPA_HN8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 25869795), '1.1803.29.00'),
    ('Caldeiraria', 'Montagem de silos metálicos de 100 m³', 'MONT', '_SILOS', 'Hora normal', '4292801', 'MONT_SILOS_HN4292801', NULL, CURRENT_TIMESTAMP - make_interval(secs => 25864203), '1.0105.50.00'),
    ('Apoio e logística', 'Locacao de caminhao munck', 'LOC', '_MUNCK', 'Obs.: Itens que não são mão de obra', '7719599', 'LOC_MUNCK_OBS7719599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 25586064), '1.1101.12.00'),
    ('Civil', 'Montagem de andaimes fachadeiros', 'ANDAIM', '_FACHAD', 'Hora normal', '4399102', 'ANDAIM_FACHAD_HN4399102', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 25152220), '1.0105.70.00'),
    ('Eletromecânica', 'Troca de eletrodos de grafite do forno', 'TROCA', '_ELETRO', 'Adicional noturno (hora)', '3314718', 'TROCA_ELETRO_NOT3314718', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 24389346), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de redutor de velocidade', 'MANUT', '_REDUTO', 'Hora normal', '3314705', 'MANUT_REDUTO_HN3314705', NULL, CURRENT_TIMESTAMP - make_interval(secs => 24379189), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Inspeção de vasos de pressão (NR-13)', 'INSP', '_NR13', 'Hora normal', '7120100', 'INSP_NR13_HN7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 24317103), '1.1404.49.00'),
    ('Inspeção e ensaios', 'Inspeção de vasos de pressão (NR-13)', 'INSP', '_NR13', 'Hora extra Dom/Feriado', '7120100', 'INSP_NR13_HED7120100', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 23869850), '1.1404.49.00'),
    ('Caldeiraria', 'Montagem de silos metálicos de 100 m³', 'MONT', '_SILOS', 'Hora extra Seg a Sab', '4292801', 'MONT_SILOS_HES4292801', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 23254052), '1.0105.50.00'),
    ('Caldeiraria', 'Manutenção de caldeira de recuperação', 'MANUT', '_CALDEI', 'Adicional noturno (hora)', '3311200', 'MANUT_CALDEI_NOT3311200', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 23248642), '1.2001.10.00'),
    ('Apoio e logística', 'Locação de plataforma elevatória 12 m', 'LOCAC', '_PLATEL', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_PLATEL_OBS7732201', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 23100244), '1.1101.30.00'),
    ('Civil', 'Montagem de andaimes fachadeiros', 'ANDAIM', '_FACHAD', 'Mobilizacao / Desmobilizacao', '4399102', 'ANDAIM_FACHAD_MOB4399102', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 23016668), '1.0105.70.00'),
    ('Apoio e logística', 'Mão de obra temporária para parada programada', 'MOTEMP', '_PARADA', 'Posto mensal (equipe fixa)', '7820500', 'MOTEMP_PARADA_POSTO7820500', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 22995916), '1.1801.22.00'),
    ('Eletromecânica', 'Manutenção de laminador de barras', 'MANUT', '_LAMBAR', 'Hora normal', '3314718', 'MANUT_LAMBAR_HN3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 22918793), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de redutor de velocidade', 'MANUT', '_REDUTO', 'Adicional noturno (hora)', '3314705', 'MANUT_REDUTO_NOT3314705', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 22897862), '1.2001.50.00'),
    ('Apoio e logística', 'Mão de obra temporária para parada programada', 'MOTEMP', '_PARADA', 'Hora extra Seg a Sab', '7820500', 'MOTEMP_PARADA_HES7820500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 22824599), '1.1801.22.00'),
    ('Caldeiraria', 'Solda de reparo em aço carbono', 'SOLDA', '_REPCAR', 'Hora normal', '2539001', 'SOLDA_REPCAR_HN2539001', NULL, CURRENT_TIMESTAMP - make_interval(secs => 22575981), '1.2001.10.00'),
    ('Apoio e logística', 'Locação de gerador diesel', 'LOCAC', '_GERDIE', 'Hora normal', '7739099', 'LOCAC_GERDIE_HN7739099', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 22226483), '1.1101.90.00'),
    ('Automação', 'Instalação de instrumentação de campo', 'INSTAL', '_CAMPO', 'Hora extra noturna Seg a Sab', '3321000', 'INSTAL_CAMPO_HENS3321000', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 21812565), '1.2003.21.90'),
    ('Caldeiraria', 'Manutenção de caldeira de recuperação', 'MANUT', '_CALDEI', 'Hora extra Dom/Feriado', '3311200', 'MANUT_CALDEI_HED3311200', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 21716285), '1.2001.10.00'),
    ('Eletromecânica', 'Manutenção de laminador de barras', 'MANUT', '_LAMBAR', 'Hora extra Seg a Sab', '3314718', 'MANUT_LAMBAR_HES3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 21607925), '1.2001.50.00'),
    ('Limpeza industrial', 'Limpeza de canaletas e fossos de carepa', 'LIMP', '_CAREPA', 'Hora normal', '8129000', 'LIMP_CAREPA_HN8129000', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 21352309), '1.1803.29.00'),
    ('Caldeiraria', 'Montagem industrial de equipamentos pesados', 'MONT', '_EQPESA', 'Hora extra Seg a Sab', '4292802', 'MONT_EQPESA_HES4292802', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 21293849), '1.2003.21.90'),
    ('Civil', 'Montagem de andaimes fachadeiros', 'ANDAIM', '_FACHAD', 'Hora extra Seg a Sab', '4399102', 'ANDAIM_FACHAD_HES4399102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 21284982), '1.0105.70.00'),
    ('Limpeza industrial', 'Limpeza de canaletas e fossos de carepa', 'LIMP', '_CAREPA', 'Adicional noturno (hora)', '8129000', 'LIMP_CAREPA_NOT8129000', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 21267205), '1.1803.29.00'),
    ('Inspeção e ensaios', 'Ensaio por líquido penetrante', 'ENSAIO', '_LIQPEN', 'Hora normal', '7120100', 'ENSAIO_LIQPEN_HN7120100', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 21104303), '1.1404.49.00'),
    ('Eletromecânica', 'Manutenção de trocador de calor', 'MANUT', '_TROCAD', 'Hora extra Dom/Feriado', '3314706', 'MANUT_TROCAD_HED3314706', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 21009429), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Ensaio por líquido penetrante', 'ENSAIO', '_LIQPEN', 'Hora extra Dom/Feriado', '7120100', 'ENSAIO_LIQPEN_HED7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 20765056), '1.1404.49.00'),
    ('Limpeza industrial', 'Aspiração industrial de poeira', 'ASPIRA', '_POEIRA', 'Adicional noturno (hora)', '8129000', 'ASPIRA_POEIRA_NOT8129000', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 20515900), '1.1803.29.00'),
    ('Limpeza industrial', 'Limpeza de canaletas e fossos de carepa', 'LIMP', '_CAREPA', 'Adicional insalubridade (hora)', '8129000', 'LIMP_CAREPA_INSAL8129000', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 20505013), '1.1803.29.00'),
    ('Apoio e logística', 'Aluguel de caminhão munck', 'ALUG', '_MUNCK', 'Obs.: Itens que não são mão de obra', '7719599', 'ALUG_MUNCK_OBS7719599', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 20170209), '1.1101.12.00'),
    ('Civil', 'Montagem de andaimes fachadeiros', 'ANDAIM', '_FACHAD', 'Hora extra Dom/Feriado', '4399102', 'ANDAIM_FACHAD_HED4399102', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 20144296), '1.0105.70.00'),
    ('Refratários', 'Montagem de tijolos refratários em carro torpedo', 'MONT', '_TORPED', 'Hora extra Dom/Feriado', '4330499', 'MONT_TORPED_HED4330499', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 19727822), '1.2001.50.00'),
    ('Limpeza industrial', 'Aspiração industrial de poeira', 'ASPIRA', '_POEIRA', 'Hora extra Seg a Sab', '8129000', 'ASPIRA_POEIRA_HES8129000', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 19718726), '1.1803.29.00'),
    ('Civil', 'Reforço estrutural com fibra de carbono', 'REFOR', '_FIBRAC', 'Hora normal', '4399199', 'REFOR_FIBRAC_HN4399199', NULL, CURRENT_TIMESTAMP - make_interval(secs => 19560480), '1.0105.90.00'),
    ('Civil', 'Instalação de guarda-corpo e escadas marinheiro', 'INSTAL', '_GUARDA', 'Hora normal', '4330402', 'INSTAL_GUARDA_HN4330402', NULL, CURRENT_TIMESTAMP - make_interval(secs => 19559010), '1.0107.50.00'),
    ('Civil', 'Reforço estrutural com fibra de carbono', 'REFOR', '_FIBRAC', 'Mobilizacao / Desmobilizacao', '4399199', 'REFOR_FIBRAC_MOB4399199', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 19533355), '1.0105.90.00'),
    ('Eletromecânica', 'Manutenção de cadeira de laminação', 'MANUT', '_CADLAM', 'Hora normal', '3314718', 'MANUT_CADLAM_HN3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 19369863), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de trocador de calor', 'MANUT', '_TROCAD', 'Hora extra Seg a Sab', '3314706', 'MANUT_TROCAD_HES3314706', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 19356525), '1.2001.50.00'),
    ('Caldeiraria', 'Soldagem de revestimento por overlay', 'SOLDA', '_OVERLA', 'Hora normal', '2539001', 'SOLDA_OVERLA_HN2539001', NULL, CURRENT_TIMESTAMP - make_interval(secs => 19128979), '1.2001.10.00'),
    ('Segurança', 'Resgate técnico em altura', 'RESGAT', '_ALTURA', 'Hora normal', '8011101', 'RESGAT_ALTURA_HN8011101', NULL, CURRENT_TIMESTAMP - make_interval(secs => 19119705), '1.1801.29.00'),
    ('Inspeção e ensaios', 'Inspeção de vasos de pressão (NR-13)', 'INSP', '_NR13', 'Hora extra Seg a Sab', '7120100', 'INSP_NR13_HES7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 19111798), '1.1404.49.00'),
    ('Refratários', 'Demolição de refratário de distribuidor', 'DEMOL', '_DISTRI', 'Hora extra Dom/Feriado', '4311801', 'DEMOL_DISTRI_HED4311801', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 18948577), '1.0103.10.00'),
    ('Refratários', 'Demolição de refratário de distribuidor', 'DEMOL', '_DISTRI', 'Hora normal', '4311801', 'DEMOL_DISTRI_HN4311801', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 18938385), '1.0103.10.00'),
    ('Civil', 'Instalação de guarda-corpo e escadas marinheiro', 'INSTAL', '_GUARDA', 'Hora extra Dom/Feriado', '4330402', 'INSTAL_GUARDA_HED4330402', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 18779451), '1.0107.50.00'),
    ('Eletromecânica', 'Instalação de eletrocalha e leito', 'INST', '_ELCALH', 'Hora normal', '4321500', 'INST_ELCALH_HN4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 18768814), '1.0106.11.00'),
    ('Civil', 'Pintura anticorrosiva estruturas metalicas', 'PINT', '_ANTICO', 'Hora normal', '4330404', 'PINT_ANTICO_HN4330404', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 18596654), '1.0107.30.00'),
    ('Eletromecânica', 'Manutenção de cadeira de laminação', 'MANUT', '_CADLAM', 'Adicional noturno (hora)', '3314718', 'MANUT_CADLAM_NOT3314718', NULL, CURRENT_TIMESTAMP - make_interval(secs => 18521356), '1.2001.50.00'),
    ('Caldeiraria', 'Manutenção de caldeira de recuperação', 'MANUT', '_CALDEI', 'Hora extra noturna Seg a Sab', '3311200', 'MANUT_CALDEI_HENS3311200', NULL, CURRENT_TIMESTAMP - make_interval(secs => 18442065), '1.2001.10.00'),
    ('Automação', 'Manutenção de sistema supervisório', 'MANUT', '_SUPERV', 'Hora normal', '6209100', 'MANUT_SUPERV_HN6209100', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 18420734), '1.2001.50.00'),
    ('Eletromecânica', 'Instalação de sistema de proteção contra incêndio', 'INSTAL', '_INCEND', 'Hora normal', '4322303', 'INSTAL_INCEND_HN4322303', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 18182125), '1.0106.12.00'),
    ('Limpeza industrial', 'Hidrojateamento de equipamentos', 'HIDROJ', '_EQUIPA', 'Hora normal', '8129000', 'HIDROJ_EQUIPA_HN8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 18163490), '1.1803.29.00'),
    ('Refratários', 'Demolição de refratário de distribuidor', 'DEMOL', '_DISTRI', 'Hora extra Seg a Sab', '4311801', 'DEMOL_DISTRI_HES4311801', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 18159415), '1.0103.10.00'),
    ('Automação', 'Instalação de instrumentação de campo', 'INSTAL', '_CAMPO', 'Hora extra Seg a Sab', '3321000', 'INSTAL_CAMPO_HES3321000', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 18153435), '1.2003.21.90'),
    ('Caldeiraria', 'Soldagem de revestimento por overlay', 'SOLDA', '_OVERLA', 'Adicional noturno (hora)', '2539001', 'SOLDA_OVERLA_NOT2539001', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 18095281), '1.2001.10.00'),
    ('Eletromecânica', 'Manutenção de trocador de calor', 'MANUT', '_TROCAD', 'Hora extra noturna Seg a Sab', '3314706', 'MANUT_TROCAD_HENS3314706', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 18091455), '1.2001.50.00'),
    ('Automação', 'Manutenção de sistema supervisório', 'MANUT', '_SUPERV', 'Hora extra Seg a Sab', '6209100', 'MANUT_SUPERV_HES6209100', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 18087569), '1.2001.50.00'),
    ('Segurança', 'Resgate técnico em altura', 'RESGAT', '_ALTURA', 'Adicional periculosidade (hora)', '8011101', 'RESGAT_ALTURA_PERIC8011101', NULL, CURRENT_TIMESTAMP - make_interval(secs => 18062495), '1.1801.29.00'),
    ('Eletromecânica', 'Manutenção de disjuntores de média tensão', 'MANUT', '_DISJUN', 'Hora normal', '3313999', 'MANUT_DISJUN_HN3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 17913927), '1.2001.50.00'),
    ('Refratários', 'Montagem de tijolos refratários em carro torpedo', 'MONT', '_TORPED', 'Hora extra Seg a Sab', '4330499', 'MONT_TORPED_HES4330499', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 17903928), '1.2001.50.00'),
    ('Caldeiraria', 'Revestimento de chapas de desgaste', 'REVEST', '_DESGAS', 'Hora normal', '2539001', 'REVEST_DESGAS_HN2539001', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 17837866), '1.2001.10.00'),
    ('Refratários', 'Montagem de tijolos refratários em carro torpedo', 'MONT', '_TORPED', 'Mobilizacao / Desmobilizacao', '4330499', 'MONT_TORPED_MOB4330499', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 17658450), '1.2001.50.00'),
    ('Segurança', 'Elaboração de laudo técnico de insalubridade', 'LAUDO', '_INSALU', 'Hora extra Dom/Feriado', '7119704', 'LAUDO_INSALU_HED7119704', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 17655313), '1.1403.90.00'),
    ('Segurança', 'Resgate técnico em altura', 'RESGAT', '_ALTURA', 'Adicional periculosidade (mensal)', '8011101', 'RESGAT_ALTURA_PERM8011101', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 17231414), '1.1801.29.00'),
    ('Eletromecânica', 'Manutenção de disjuntores de média tensão', 'MANUT', '_DISJUN', 'Adicional noturno (hora)', '3313999', 'MANUT_DISJUN_NOT3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 17230385), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de subestação elétrica', 'MANUT', '_SUBEST', 'Adicional periculosidade (mensal)', '3313999', 'MANUT_SUBEST_PERM3313999', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 17220447), '1.2001.50.00'),
    ('Tubulação', 'Limpeza química de linhas', 'LIMQUI', '_LINHAS', 'Hora normal', '8129000', 'LIMQUI_LINHAS_HN8129000', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 17193928), '1.1803.29.00'),
    ('Eletromecânica', 'Instalação de sistema de proteção contra incêndio', 'INSTAL', '_INCEND', 'Hora extra Dom/Feriado', '4322303', 'INSTAL_INCEND_HED4322303', NULL, CURRENT_TIMESTAMP - make_interval(secs => 17025030), '1.0106.12.00'),
    ('Tubulação', 'Limpeza química de linhas', 'LIMQUI', '_LINHAS', 'Hora extra Seg a Sab', '8129000', 'LIMQUI_LINHAS_HES8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16940706), '1.1803.29.00'),
    ('Civil', 'Instalação de guarda-corpo e escadas marinheiro', 'INSTAL', '_GUARDA', 'Hora extra Seg a Sab', '4330402', 'INSTAL_GUARDA_HES4330402', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16793191), '1.0107.50.00'),
    ('Refratários', 'Demolição de refratário de distribuidor', 'DEMOL', '_DISTRI', 'Mobilizacao / Desmobilizacao', '4311801', 'DEMOL_DISTRI_MOB4311801', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 16676211), '1.0103.10.00'),
    ('Civil', 'Impermeabilização de lajes de cobertura', 'IMPERM', '_LAJECO', 'Hora normal', '4330401', 'IMPERM_LAJECO_HN4330401', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16595225), '1.0105.30.00'),
    ('Eletromecânica', 'Manutenção de disjuntores de média tensão', 'MANUT', '_DISJUN', 'Adicional periculosidade (hora)', '3313999', 'MANUT_DISJUN_PERIC3313999', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16433575), '1.2001.50.00'),
    ('Limpeza industrial', 'Limpeza de tanques e reservatórios', 'LIMP', '_TANRES', 'Hora normal', '8129000', 'LIMP_TANRES_HN8129000', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 16192784), '1.1803.29.00'),
    ('Limpeza industrial', 'Hidrojateamento de equipamentos', 'HIDROJ', '_EQUIPA', 'Adicional insalubridade (hora)', '8129000', 'HIDROJ_EQUIPA_INSAL8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16183698), '1.1803.29.00'),
    ('Caldeiraria', 'Soldagem de revestimento por overlay', 'SOLDA', '_OVERLA', 'Hora extra Seg a Sab', '2539001', 'SOLDA_OVERLA_HES2539001', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 16158239), '1.2001.10.00'),
    ('Eletromecânica', 'Alinhamento a laser de acionamentos', 'ALINH', '_LASER', 'Hora normal', '3314710', 'ALINH_LASER_HN3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 16097786), '1.2001.50.00'),
    ('Automação', 'Instalação de câmeras industriais CFTV', 'INSTAL', '_CFTV', 'Hora normal', '4321500', 'INSTAL_CFTV_HN4321500', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 16008402), '1.0106.13.00'),
    ('Automação', 'Manutenção de analisadores de gases', 'MANUT', '_ANALIS', 'Hora normal', '3312102', 'MANUT_ANALIS_HN3312102', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 15837859), '1.2001.50.00'),
    ('Eletromecânica', 'Termografia de painéis elétricos', 'TERMOG', '_PAINEI', 'Hora normal', '7120100', 'TERMOG_PAINEI_HN7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 15735992), '1.1404.43.00'),
    ('Automação', 'Instalação de instrumentação de campo', 'INSTAL', '_CAMPO', 'Hora extra Dom/Feriado', '3321000', 'INSTAL_CAMPO_HED3321000', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 15675948), '1.2003.21.90'),
    ('Eletromecânica', 'Termografia de painéis elétricos', 'TERMOG', '_PAINEI', 'Hora extra Dom/Feriado', '7120100', 'TERMOG_PAINEI_HED7120100', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 15589199), '1.1404.43.00'),
    ('Limpeza industrial', 'Remoção de resíduos de escória', 'REMOCA', '_ESCORI', 'Hora normal', '3821100', 'REMOCA_ESCORI_HN3821100', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 15562308), '1.1803.29.00'),
    ('Tubulação', 'Limpeza química de linhas', 'LIMQUI', '_LINHAS', 'Adicional insalubridade (hora)', '8129000', 'LIMQUI_LINHAS_INSAL8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 15494687), '1.1803.29.00'),
    ('Civil', 'Instalação de guarda-corpo e escadas marinheiro', 'INSTAL', '_GUARDA', 'Mobilizacao / Desmobilizacao', '4330402', 'INSTAL_GUARDA_MOB4330402', NULL, CURRENT_TIMESTAMP - make_interval(secs => 15413586), '1.0107.50.00'),
    ('Caldeiraria', 'Revestimento de chapas de desgaste', 'REVEST', '_DESGAS', 'Hora extra Seg a Sab', '2539001', 'REVEST_DESGAS_HES2539001', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 15241158), '1.2001.10.00'),
    ('Eletromecânica', 'Alinhamento a laser de acionamentos', 'ALINH', '_LASER', 'Hora extra noturna Seg a Sab', '3314710', 'ALINH_LASER_HENS3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 15215487), '1.2001.50.00'),
    ('Caldeiraria', 'Reparo de tanque de armazenamento', 'REPAR', '_TANARM', 'Hora normal', '3311200', 'REPAR_TANARM_HN3311200', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 15061642), '1.2001.10.00'),
    ('Limpeza industrial', 'Remoção de resíduos de escória', 'REMOCA', '_ESCORI', 'Hora extra Seg a Sab', '3821100', 'REMOCA_ESCORI_HES3821100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 15058110), '1.1803.29.00'),
    ('Automação', 'Manutenção de analisadores de gases', 'MANUT', '_ANALIS', 'Adicional noturno (hora)', '3312102', 'MANUT_ANALIS_NOT3312102', NULL, CURRENT_TIMESTAMP - make_interval(secs => 14956745), '1.2001.50.00'),
    ('Limpeza industrial', 'Limpeza de tanques e reservatórios', 'LIMP', '_TANRES', 'Hora extra Seg a Sab', '8129000', 'LIMP_TANRES_HES8129000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 14723273), '1.1803.29.00'),
    ('Eletromecânica', 'Rebobinamento de máquinas elétricas', 'REBOB', '_MAQELE', 'Hora normal', '3313901', 'REBOB_MAQELE_HN3313901', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 14710842), '1.2001.50.00'),
    ('Automação', 'Instalação de câmeras industriais CFTV', 'INSTAL', '_CFTV', 'Hora extra Dom/Feriado', '4321500', 'INSTAL_CFTV_HED4321500', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 14542854), '1.0106.13.00'),
    ('Civil', 'Impermeabilização de lajes de cobertura', 'IMPERM', '_LAJECO', 'Hora extra Dom/Feriado', '4330401', 'IMPERM_LAJECO_HED4330401', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 14531683), '1.0105.30.00'),
    ('Civil', 'Impermeabilização de lajes de cobertura', 'IMPERM', '_LAJECO', 'Mobilizacao / Desmobilizacao', '4330401', 'IMPERM_LAJECO_MOB4330401', NULL, CURRENT_TIMESTAMP - make_interval(secs => 14374531), '1.0105.30.00'),
    ('Civil', 'Execução de paredes de vedação em blocos', 'PAREDE', '_VEDACA', 'Hora extra Seg a Sab', '4399103', 'PAREDE_VEDACA_HES4399103', NULL, CURRENT_TIMESTAMP - make_interval(secs => 14095157), '1.0105.60.00'),
    ('Eletromecânica', 'Rebobinamento de máquinas elétricas', 'REBOB', '_MAQELE', 'Hora extra Seg a Sab', '3313901', 'REBOB_MAQELE_HES3313901', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 13661317), '1.2001.50.00'),
    ('Eletromecânica', 'Rebobinamento de máquinas elétricas', 'REBOB', '_MAQELE', 'Adicional noturno (hora)', '3313901', 'REBOB_MAQELE_NOT3313901', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 13603457), '1.2001.50.00'),
    ('Caldeiraria', 'Reparo de tanque de armazenamento', 'REPAR', '_TANARM', 'Hora extra Dom/Feriado', '3311200', 'REPAR_TANARM_HED3311200', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 13574294), '1.2001.10.00'),
    ('Segurança', 'Elaboração de laudo técnico de insalubridade', 'LAUDO', '_INSALU', 'Hora extra Seg a Sab', '7119704', 'LAUDO_INSALU_HES7119704', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 13336925), '1.1403.90.00'),
    ('Civil', 'Construção de canaleta de drenagem', 'CONST', '_CANALE', 'Hora normal', '4399199', 'CONST_CANALE_HN4399199', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 12882438), '1.0105.90.00'),
    ('Civil', 'Pintura anticorrosiva de estruturas metálicas', 'PINT', '_ESTMET', 'Hora normal', '4330404', 'PINT_ESTMET_HN4330404', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 12809247), '1.0107.30.00'),
    ('Eletromecânica', 'Manutenção de forno elétrico a arco', 'MANUT', '_FORNEA', 'Hora extra noturna Seg a Sab', '3314718', 'MANUT_FORNEA_HENS3314718', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 11778098), '1.2001.50.00'),
    ('Civil', 'Construção de canaleta de drenagem', 'CONST', '_CANALE', 'Hora extra Seg a Sab', '4399199', 'CONST_CANALE_HES4399199', NULL, CURRENT_TIMESTAMP - make_interval(secs => 11616130), '1.0105.90.00'),
    ('Eletromecânica', 'Alinhamento a laser de acionamentos', 'ALINH', '_LASER', 'Hora extra Dom/Feriado', '3314710', 'ALINH_LASER_HED3314710', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 11589350), '1.2001.50.00'),
    ('Civil', 'Execução de paredes de vedação em blocos', 'PAREDE', '_VEDACA', 'Mobilizacao / Desmobilizacao', '4399103', 'PAREDE_VEDACA_MOB4399103', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 11587873), '1.0105.60.00'),
    ('Civil', 'Reparo de pavimento intertravado', 'REPAR', '_PAVINT', 'Mobilizacao / Desmobilizacao', '4211101', 'REPAR_PAVINT_MOB4211101', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 11330590), '1.0102.11.00'),
    ('Civil', 'Pintura anticorrosiva de estruturas metálicas', 'PINT', '_ESTMET', 'Hora extra Dom/Feriado', '4330404', 'PINT_ESTMET_HED4330404', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 11160891), '1.0107.30.00'),
    ('Refratários', 'Reparo de revestimento refratário de panela de aço', 'REFRAT', '_PANELA', 'Hora normal', '4330499', 'REFRAT_PANELA_HN4330499', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 10923627), '1.2001.50.00'),
    ('Eletromecânica', 'Alinhamento a laser de acionamentos', 'ALINH', '_LASER', 'Hora extra Seg a Sab', '3314710', 'ALINH_LASER_HES3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 10920088), '1.2001.50.00'),
    ('Civil', 'Reparo de pavimento intertravado', 'REPAR', '_PAVINT', 'Hora extra Seg a Sab', '4211101', 'REPAR_PAVINT_HES4211101', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 10395876), '1.0102.11.00'),
    ('Civil', 'Montagem de cobertura metálica', 'COBERT', '_TELHAM', 'Hora normal', '4330499', 'COBERT_TELHAM_HN4330499', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 10282519), '1.0105.22.00'),
    ('Refratários', 'Projeção de concreto refratário', 'PROJEC', '_CONREF', 'Hora extra Dom/Feriado', '4330499', 'PROJEC_CONREF_HED4330499', NULL, CURRENT_TIMESTAMP - make_interval(secs => 10234997), '1.2001.50.00'),
    ('Civil', 'Terraplenagem de pátio de estocagem', 'TERRAP', '_PATIOE', 'Hora normal', '4313400', 'TERRAP_PATIOE_HN4313400', NULL, CURRENT_TIMESTAMP - make_interval(secs => 10030031), '1.0103.30.00'),
    ('Tubulação', 'Instalação de linhas de vapor e condensado', 'INSTAL', '_VAPOR', 'Hora normal', '3329599', 'INSTAL_VAPOR_HN3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 10022878), '1.0106.90.00'),
    ('Civil', 'Pintura anticorrosiva de estruturas metálicas', 'PINT', '_ESTMET', 'Hora extra Seg a Sab', '4330404', 'PINT_ESTMET_HES4330404', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 9875759), '1.0107.30.00'),
    ('Eletromecânica', 'Manutenção de ponte rolante', 'MANUT', '_PONTER', 'Hora normal', '3314710', 'MANUT_PONTER_HN3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 9780333), '1.2001.50.00'),
    ('Civil', 'Construção de canaleta de drenagem', 'CONST', '_CANALE', 'Hora extra Dom/Feriado', '4399199', 'CONST_CANALE_HED4399199', NULL, CURRENT_TIMESTAMP - make_interval(secs => 9623477), '1.0105.90.00'),
    ('Civil', 'Construção de canaleta de drenagem', 'CONST', '_CANALE', 'Mobilizacao / Desmobilizacao', '4399199', 'CONST_CANALE_MOB4399199', NULL, CURRENT_TIMESTAMP - make_interval(secs => 9514493), '1.0105.90.00'),
    ('Refratários', 'Reparo de revestimento refratário de panela de aço', 'REFRAT', '_PANELA', 'Mobilizacao / Desmobilizacao', '4330499', 'REFRAT_PANELA_MOB4330499', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 9350513), '1.2001.50.00'),
    ('Civil', 'Montagem de cobertura metálica', 'COBERT', '_TELHAM', 'Hora extra Dom/Feriado', '4330499', 'COBERT_TELHAM_HED4330499', NULL, CURRENT_TIMESTAMP - make_interval(secs => 9110052), '1.0105.22.00'),
    ('Civil', 'Impermeabilização de lajes de cobertura', 'IMPERM', '_LAJECO', 'Hora extra Seg a Sab', '4330401', 'IMPERM_LAJECO_HES4330401', NULL, CURRENT_TIMESTAMP - make_interval(secs => 9095372), '1.0105.30.00'),
    ('Eletromecânica', 'Manutenção de ponte rolante', 'MANUT', '_PONTER', 'Adicional noturno (hora)', '3314710', 'MANUT_PONTER_NOT3314710', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 8918396), '1.2001.50.00'),
    ('Tubulação', 'Instalação de linhas de vapor e condensado', 'INSTAL', '_VAPOR', 'Hora extra Seg a Sab', '3329599', 'INSTAL_VAPOR_HES3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 8644673), '1.0106.90.00'),
    ('Automação', 'Lançamento e conectorização de fibra óptica', 'LANFIB', '_FIBRA', 'Hora normal', '4321500', 'LANFIB_FIBRA_HN4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 8570457), '1.0106.19.00'),
    ('Automação', 'Lançamento e conectorização de fibra óptica', 'LANFIB', '_FIBRA', 'Hora extra Seg a Sab', '4321500', 'LANFIB_FIBRA_HES4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 8564335), '1.0106.19.00'),
    ('Civil', 'Montagem de cobertura metálica', 'COBERT', '_TELHAM', 'Hora extra Seg a Sab', '4330499', 'COBERT_TELHAM_HES4330499', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 8475117), '1.0105.22.00'),
    ('Caldeiraria', 'Fabricação de estruturas para pontes de tubulação', 'FABRIC', '_ESTPON', 'Hora normal', '2511000', 'FABRIC_ESTPON_HN2511000', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 8318926), '1.0105.50.00'),
    ('Tubulação', 'Instalação de linhas de vapor e condensado', 'INSTAL', '_VAPOR', 'Hora extra noturna Seg a Sab', '3329599', 'INSTAL_VAPOR_HENS3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 8297257), '1.0106.90.00'),
    ('Apoio e logística', 'Fornecimento de equipe de apoio operacional', 'EQAPOI', '_OPERAC', 'Posto mensal (equipe fixa)', '7830200', 'EQAPOI_OPERAC_POSTO7830200', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 8243163), '1.1801.21.00'),
    ('Civil', 'Terraplenagem de pátio de estocagem', 'TERRAP', '_PATIOE', 'Hora extra Dom/Feriado', '4313400', 'TERRAP_PATIOE_HED4313400', NULL, CURRENT_TIMESTAMP - make_interval(secs => 7901732), '1.0103.30.00'),
    ('Caldeiraria', 'Fabricação de estruturas para pontes de tubulação', 'FABRIC', '_ESTPON', 'Hora extra Dom/Feriado', '2511000', 'FABRIC_ESTPON_HED2511000', NULL, CURRENT_TIMESTAMP - make_interval(secs => 7864781), '1.0105.50.00'),
    ('Eletromecânica', 'Manutenção de ponte rolante', 'MANUT', '_PONTER', 'Hora extra Dom/Feriado', '3314710', 'MANUT_PONTER_HED3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 7814844), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de ponte rolante', 'MANUT', '_PONTER', 'Hora extra Seg a Sab', '3314710', 'MANUT_PONTER_HES3314710', NULL, CURRENT_TIMESTAMP - make_interval(secs => 7453810), '1.2001.50.00'),
    ('Refratários', 'Reparo de revestimento refratário de panela de aço', 'REFRAT', '_PANELA', 'Hora extra Seg a Sab', '4330499', 'REFRAT_PANELA_HES4330499', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 7294020), '1.2001.50.00'),
    ('Eletromecânica', 'Ensaios em transformadores de potência', 'ENSAIO', '_TRAFOS', 'Hora normal', '7120100', 'ENSAIO_TRAFOS_HN7120100', NULL, CURRENT_TIMESTAMP - make_interval(secs => 7293660), '1.1404.43.00'),
    ('Tubulação', 'Instalação de válvulas de bloqueio em linhas de processo', 'INSTAL', '_BLOQUE', 'Hora normal', '3329599', 'INSTAL_BLOQUE_HN3329599', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 7263962), '1.0106.90.00'),
    ('Inspeção e ensaios', 'Inspeção por ultrassom em soldas', 'INSP', '_ULTRAS', 'Hora extra Seg a Sab', '7120100', 'INSP_ULTRAS_HES7120100', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 7178112), '1.1404.49.00'),
    ('Tubulação', 'Instalação de válvulas de bloqueio em linhas de processo', 'INSTAL', '_BLOQUE', 'Hora extra noturna Seg a Sab', '3329599', 'INSTAL_BLOQUE_HENS3329599', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 6743624), '1.0106.90.00'),
    ('Tubulação', 'Montagem de suportes e apoios de tubulação', 'MONT', '_SUPORT', 'Hora normal', '4292802', 'MONT_SUPORT_HN4292802', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 6080638), '1.0105.50.00'),
    ('Tubulação', 'Instalação de válvulas de bloqueio em linhas de processo', 'INSTAL', '_BLOQUE', 'Hora extra Dom/Feriado', '3329599', 'INSTAL_BLOQUE_HED3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 5972029), '1.0106.90.00'),
    ('Civil', 'Montagem de cobertura metálica', 'COBERT', '_TELHAM', 'Mobilizacao / Desmobilizacao', '4330499', 'COBERT_TELHAM_MOB4330499', NULL, CURRENT_TIMESTAMP - make_interval(secs => 5794243), '1.0105.22.00'),
    ('Tubulação', 'Instalação de linhas de vapor e condensado', 'INSTAL', '_VAPOR', 'Hora extra Dom/Feriado', '3329599', 'INSTAL_VAPOR_HED3329599', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 5649862), '1.0106.90.00'),
    ('Eletromecânica', 'Ensaios em transformadores de potência', 'ENSAIO', '_TRAFOS', 'Hora extra Dom/Feriado', '7120100', 'ENSAIO_TRAFOS_HED7120100', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 5552368), '1.1404.43.00'),
    ('Automação', 'Lançamento e conectorização de fibra óptica', 'LANFIB', '_FIBRA', 'Hora extra noturna Seg a Sab', '4321500', 'LANFIB_FIBRA_HENS4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 5197070), '1.0106.19.00'),
    ('Civil', 'Terraplenagem de pátio de estocagem', 'TERRAP', '_PATIOE', 'Mobilizacao / Desmobilizacao', '4313400', 'TERRAP_PATIOE_MOB4313400', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 5045583), '1.0103.30.00'),
    ('Eletromecânica', 'Ensaios em transformadores de potência', 'ENSAIO', '_TRAFOS', 'Hora extra Seg a Sab', '7120100', 'ENSAIO_TRAFOS_HES7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 4696452), '1.1404.43.00'),
    ('Tubulação', 'Montagem de suportes e apoios de tubulação', 'MONT', '_SUPORT', 'Hora extra Dom/Feriado', '4292802', 'MONT_SUPORT_HED4292802', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 4270742), '1.0105.50.00'),
    ('Inspeção e ensaios', 'Levantamento topográfico de obras', 'TOPOGR', '_OBRAS', 'Hora extra Seg a Sab', '7112000', 'TOPOGR_OBRAS_HES7112000', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 4255648), '1.1404.21.00'),
    ('Automação', 'Lançamento e conectorização de fibra óptica', 'LANFIB', '_FIBRA', 'Hora extra Dom/Feriado', '4321500', 'LANFIB_FIBRA_HED4321500', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3893901), '1.0106.19.00'),
    ('Civil', 'Escavação mecanizada de valas', 'ESCAV', '_VALAS', 'Hora normal', '4313400', 'ESCAV_VALAS_HN4313400', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 3848639), '1.0103.30.00'),
    ('Automação', 'Instalação de instrumentação de campo', 'INSTAL', '_CAMPO', 'Hora normal', '3321000', 'INSTAL_CAMPO_HN3321000', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 3766937), '1.2003.21.90'),
    ('Inspeção e ensaios', 'Inspeção por ultrassom em soldas', 'INSP', '_ULTRAS', 'Hora normal', '7120100', 'INSP_ULTRAS_HN7120100', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 3725610), '1.1404.49.00'),
    ('Refratários', 'Secagem e aquecimento de revestimento refratário', 'SECAG', '_AQUECI', 'Hora normal', '3314718', 'SECAG_AQUECI_HN3314718', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 3646468), '1.2001.50.00'),
    ('Eletromecânica', 'Instalação de sistema de aterramento', 'INSTAL', '_ATERRA', 'Hora normal', '4321500', 'INSTAL_ATERRA_HN4321500', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 3613180), '1.0106.19.00'),
    ('Eletromecânica', 'Manutenção de subestação elétrica', 'MANUT', '_SUBEST', 'Hora normal', '3313999', 'MANUT_SUBEST_HN3313999', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 3222817), '1.2001.50.00'),
    ('Limpeza industrial', 'Aspiração industrial de poeira', 'ASPIRA', '_POEIRA', 'Hora normal', '8129000', 'ASPIRA_POEIRA_HN8129000', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 3176379), '1.1803.29.00'),
    ('Civil', 'Execução de paredes de vedação em blocos', 'PAREDE', '_VEDACA', 'Hora normal', '4399103', 'PAREDE_VEDACA_HN4399103', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 3007381), '1.0105.60.00'),
    ('Refratários', 'Montagem de tijolos refratários em carro torpedo', 'MONT', '_TORPED', 'Hora normal', '4330499', 'MONT_TORPED_HN4330499', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 2735191), '1.2001.50.00'),
    ('Inspeção e ensaios', 'Levantamento topográfico de obras', 'TOPOGR', '_OBRAS', 'Hora extra Dom/Feriado', '7112000', 'TOPOGR_OBRAS_HED7112000', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 2619807), '1.1404.21.00'),
    ('Tubulação', 'Instalação de válvulas de bloqueio em linhas de processo', 'INSTAL', '_BLOQUE', 'Hora extra Seg a Sab', '3329599', 'INSTAL_BLOQUE_HES3329599', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2367367), '1.0106.90.00'),
    ('Apoio e logística', 'Locação de plataforma elevatória 20 m', 'LOCAC', '_PLATE2', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_PLATE2_OBS7732201', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 2262223), '1.1101.30.00'),
    ('Civil', 'Montagem de estrutura metálica de galpão', 'MONT', '_GALPAO', 'Hora extra Seg a Sab', '4292801', 'MONT_GALPAO_HES4292801', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 2252707), '1.0105.50.00'),
    ('Caldeiraria', 'Revestimento de chapas de desgaste', 'REVEST', '_DESGAS', 'Adicional insalubridade (hora)', '2539001', 'REVEST_DESGAS_INSAL2539001', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 2204792), '1.2001.10.00'),
    ('Segurança', 'Elaboração de laudo técnico de insalubridade', 'LAUDO', '_INSALU', 'Hora normal', '7119704', 'LAUDO_INSALU_HN7119704', 'juliana.prado', CURRENT_TIMESTAMP - make_interval(secs => 1916809), '1.1403.90.00'),
    ('Eletromecânica', 'Manutenção de trocador de calor', 'MANUT', '_TROCAD', 'Hora normal', '3314706', 'MANUT_TROCAD_HN3314706', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 1774250), '1.2001.50.00'),
    ('Limpeza industrial', 'Aspiração industrial de poeira', 'ASPIRA', '_POEIRA', 'Adicional insalubridade (hora)', '8129000', 'ASPIRA_POEIRA_INSAL8129000', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 1514934), '1.1803.29.00'),
    ('Caldeiraria', 'Montagem industrial de equipamentos pesados', 'MONT', '_EQPESA', 'Hora normal', '4292802', 'MONT_EQPESA_HN4292802', 'rafael.teixeira', CURRENT_TIMESTAMP - make_interval(secs => 1490059), '1.2003.21.90'),
    ('Inspeção e ensaios', 'Levantamento topográfico de obras', 'TOPOGR', '_OBRAS', 'Hora normal', '7112000', 'TOPOGR_OBRAS_HN7112000', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 1408114), '1.1404.21.00'),
    ('Apoio e logística', 'Locação de gerador diesel', 'LOCAC', '_GERDIE', 'Obs.: Itens que não são mão de obra', '7739099', 'LOCAC_GERDIE_OBS7739099', 'leonardo.longaray', CURRENT_TIMESTAMP - make_interval(secs => 1255235), '1.1101.90.00'),
    ('Refratários', 'Projeção de concreto refratário', 'PROJEC', '_CONREF', 'Hora normal', '4330499', 'PROJEC_CONREF_HN4330499', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 1205231), '1.2001.50.00'),
    ('Caldeiraria', 'Soldagem de revestimento por overlay', 'SOLDA', '_OVERLA', 'Adicional insalubridade (hora)', '2539001', 'SOLDA_OVERLA_INSAL2539001', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 1045374), '1.2001.10.00'),
    ('Apoio e logística', 'Locação de guindaste hidráulico 80 t', 'LOCAC', '_GUIN80', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_GUIN80_OBS7732201', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 943381), '1.1101.30.00'),
    ('Eletromecânica', 'Manutenção de forno elétrico a arco', 'MANUT', '_FORNEA', 'Hora normal', '3314718', 'MANUT_FORNEA_HN3314718', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 874371), '1.2001.50.00'),
    ('Civil', 'Execução de bases de equipamentos em concreto', 'BASEEQ', '_EQUIPA', 'Hora normal', '4399103', 'BASEEQ_EQUIPA_HN4399103', 'carlos.menezes', CURRENT_TIMESTAMP - make_interval(secs => 838259), '1.0105.40.00'),
    ('Caldeiraria', 'Montagem de silos metálicos de 200 m³', 'MONT', '_SILOS2', 'Hora normal', '4292801', 'MONT_SILOS2_HN4292801', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 658960), '1.0105.50.00'),
    ('Civil', 'Reparo de pavimento intertravado', 'REPAR', '_PAVINT', 'Hora normal', '4211101', 'REPAR_PAVINT_HN4211101', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 589873), '1.0102.11.00'),
    ('Civil', 'Demarcação de áreas e faixas de segurança', 'DEMARC', '_FAIXAS', 'Hora normal', '4330404', 'DEMARC_FAIXAS_HN4330404', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518700), '1.0107.30.00'),
    ('Civil', 'Recuperação de sistema de drenagem pluvial', 'RECUP', '_DRENAG', 'Hora normal', '4399199', 'RECUP_DRENAG_HN4399199', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518640), '1.0105.90.00'),
    ('Caldeiraria', 'Fabricação de grades de piso metálicas', 'FABRIC', '_GRADES', 'Hora normal', '2511000', 'FABRIC_GRADES_HN2511000', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518580), '1.2001.10.00'),
    ('Eletromecânica', 'Substituição de rolamentos de mesa de rolos', 'SUBST', '_ROLAMI', 'Hora extra Seg a Sab', '3314705', 'SUBST_ROLAMI_HES3314705', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518520), '1.2001.50.00'),
    ('Eletromecânica', 'Manutenção de monovias e talhas elétricas', 'MANUT', '_MONOVI', 'Hora normal', '3314710', 'MANUT_MONOVI_HN3314710', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518460), '1.2001.50.00'),
    ('Eletromecânica', 'Instalação de sistema de exaustão em cabine de pintura', 'INSTAL', '_EXAUST', 'Hora normal', '4322302', 'INSTAL_EXAUST_HN4322302', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 518400), '1.0106.32.00'),
    ('Caldeiraria', 'Usinagem de peças de reposição', 'USINAG', '_PECASR', 'Hora normal', '2539001', 'USINAG_PECASR_HN2539001', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 478057), '1.2001.10.00'),
    ('Caldeiraria', 'Manutenção de caldeira de recuperação', 'MANUT', '_CALDEI', 'Hora normal', '3311200', 'MANUT_CALDEI_HN3311200', 'fernanda.lopes', CURRENT_TIMESTAMP - make_interval(secs => 471890), '1.2001.10.00'),
    ('Eletromecânica', 'Balanceamento dinâmico de ventiladores', 'BALAN', '_VENTIL', 'Hora extra noturna Seg a Sab', '3314710', 'BALAN_VENTIL_HENS3314710', 'thiago.almeida', CURRENT_TIMESTAMP - make_interval(secs => 359968), '1.2001.50.00'),
    ('Refratários', 'Manutenção de válvula gaveta de panela', 'MANUT', '_GAVETA', 'Hora extra noturna Seg a Sab', '3314703', 'MANUT_GAVETA_HENS3314703', 'bruno.fernandes', CURRENT_TIMESTAMP - make_interval(secs => 323775), '1.2001.50.00');

-- Histórico de consultas de cadastro
INSERT INTO consultas_cadastro (criado_em, usuario, texto_breve, condicao, cnae, codigo_gerado, resultado, decisao, servico_id, justificativa)
VALUES
    (CURRENT_TIMESTAMP - make_interval(secs => 3848639), 'comprador', 'Escavação mecanizada de valas', 'Hora normal', '4313400', 'ESCAV_VALAS_HN4313400', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'ESCAV_VALAS_HN4313400'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3766937), 'comprador', 'Instalação de instrumentação de campo', 'Hora normal', '3321000', 'INSTAL_CAMPO_HN3321000', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_CAMPO_HN3321000'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3725610), 'carlos.menezes', 'Inspeção por ultrassom em soldas', 'Hora normal', '7120100', 'INSP_ULTRAS_HN7120100', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSP_ULTRAS_HN7120100'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3646468), 'bruno.fernandes', 'Secagem e aquecimento de revestimento refratário', 'Hora normal', '3314718', 'SECAG_AQUECI_HN3314718', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'SECAG_AQUECI_HN3314718'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3613180), 'rafael.teixeira', 'Instalação de sistema de aterramento', 'Hora normal', '4321500', 'INSTAL_ATERRA_HN4321500', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_ATERRA_HN4321500'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3418943), 'mariana.souza', 'Munck para içamento', 'Obs.: Itens que não são mão de obra', '7719599', 'LOCAC_MUNCK_OBS7719599', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_MUNCK_OBS7719599'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3381071), 'thiago.almeida', 'Manutenção da ponte rolante', 'Hora normal', '3314710', 'MANUT_PONTER_HN3314710', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_PONTER_HN3314710'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3282996), 'leonardo.longaray', 'Manutenção de compressor de ar comprimido', 'Hora normal', '3314704', 'MANUT_COMPR2_HN3314704', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3222817), 'thiago.almeida', 'Manutenção de subestação elétrica', 'Hora normal', '3313999', 'MANUT_SUBEST_HN3313999', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_SUBEST_HN3313999'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3176379), 'comprador', 'Aspiração industrial de poeira', 'Hora normal', '8129000', 'ASPIRA_POEIRA_HN8129000', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'ASPIRA_POEIRA_HN8129000'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3093124), 'leonardo.longaray', 'Limpeza industrial de laminadores', 'Hora normal', '8129000', 'LIMP_LAMIN2_HN8129000', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3047128), 'mariana.souza', 'Linha de ar comprimido', 'Hora normal', '3329599', 'INSTAL_ARCOMP_HN3329599', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_ARCOMP_HN3329599'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3007381), 'juliana.prado', 'Execução de paredes de vedação em blocos', 'Hora normal', '4399103', 'PAREDE_VEDACA_HN4399103', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'PAREDE_VEDACA_HN4399103'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 3005316), 'juliana.prado', 'Manutenção do redutor de velocidade', 'Hora normal', '3314705', 'MANUT_REDUT3_HN3314705', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2735191), 'fernanda.lopes', 'Montagem de tijolos refratários em carro torpedo', 'Hora normal', '4330499', 'MONT_TORPED_HN4330499', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MONT_TORPED_HN4330499'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2613837), 'leonardo.longaray', 'Aterramento de equipamentos', 'Hora normal', '4321500', 'INSTAL_ATERRA_HN4321500', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_ATERRA_HN4321500'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2583699), 'bruno.fernandes', 'Pintura anticorrosiva de estrutura metálica', 'Hora normal', '4330404', 'PINT_ESTME2_HN4330404', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2549387), 'carlos.menezes', 'Manutenção em trocadores de calor', 'Hora normal', '3314706', 'MANUT_TROCAD_HN3314706', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_TROCAD_HN3314706'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2262343), 'leonardo.longaray', 'Locação de plataforma elevatória 20 m', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_PLATE2_OBS7732201', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2262223), 'leonardo.longaray', 'Locação de plataforma elevatória 20 m', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_PLATE2_OBS7732201', 'CADASTRADO', 'CONTESTOU_BLOQUEIO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_PLATE2_OBS7732201'), 'Plataforma de 20 m para a manutenção da cobertura; a de 12 m não chega.'),
    (CURRENT_TIMESTAMP - make_interval(secs => 2252797), 'carlos.menezes', 'Montagem de estrutura metálica de galpão', 'Hora extra Seg a Sab', '4292801', 'MONT_GALPAO_HES4292801', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2252707), 'carlos.menezes', 'Montagem de estrutura metálica de galpão', 'Hora extra Seg a Sab', '4292801', 'MONT_GALPAO_HES4292801', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MONT_GALPAO_HES4292801'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2204882), 'comprador', 'Revestimento de chapas de desgaste', 'Adicional insalubridade (hora)', '2539001', 'REVEST_DESGAS_INSAL2539001', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2204792), 'comprador', 'Revestimento de chapas de desgaste', 'Adicional insalubridade (hora)', '2539001', 'REVEST_DESGAS_INSAL2539001', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'REVEST_DESGAS_INSAL2539001'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 2078018), 'rafael.teixeira', 'Teste hidrostático', 'Hora normal', '7120100', 'TESTEH_HIDROS_HN7120100', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'TESTEH_HIDROS_HN7120100'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1936323), 'comprador', 'Inspeção por ultrassom', 'Hora normal', '7120100', 'INSP_ULTRAS_HN7120100', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSP_ULTRAS_HN7120100'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1925870), 'leonardo.longaray', 'Troca de eletrodo de grafite do forno', 'Hora normal', '3314718', 'TROCA_ELETR2_HN3314718', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1916809), 'juliana.prado', 'Elaboração de laudo técnico de insalubridade', 'Hora normal', '7119704', 'LAUDO_INSALU_HN7119704', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LAUDO_INSALU_HN7119704'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1774250), 'rafael.teixeira', 'Manutenção de trocador de calor', 'Hora normal', '3314706', 'MANUT_TROCAD_HN3314706', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_TROCAD_HN3314706'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1523785), 'mariana.souza', 'Análise de vibração em equipamentos rotativos', 'Hora normal', '7120100', 'ANAVIB_ROTAT2_HN7120100', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1515024), 'comprador', 'Aspiração industrial de poeira', 'Adicional insalubridade (hora)', '8129000', 'ASPIRA_POEIRA_INSAL8129000', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1514934), 'comprador', 'Aspiração industrial de poeira', 'Adicional insalubridade (hora)', '8129000', 'ASPIRA_POEIRA_INSAL8129000', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'ASPIRA_POEIRA_INSAL8129000'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1490059), 'rafael.teixeira', 'Montagem industrial de equipamentos pesados', 'Hora normal', '4292802', 'MONT_EQPESA_HN4292802', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MONT_EQPESA_HN4292802'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1408114), 'thiago.almeida', 'Levantamento topográfico de obras', 'Hora normal', '7112000', 'TOPOGR_OBRAS_HN7112000', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'TOPOGR_OBRAS_HN7112000'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1367755), 'carlos.menezes', 'Instalação de eletrocalhas', 'Hora normal', '4321500', 'INSTAL_CALHA2_HN4321500', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1255235), 'leonardo.longaray', 'Locação de gerador diesel', 'Obs.: Itens que não são mão de obra', '7739099', 'LOCAC_GERDIE_OBS7739099', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_GERDIE_OBS7739099'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1205231), 'carlos.menezes', 'Projeção de concreto refratário', 'Hora normal', '4330499', 'PROJEC_CONREF_HN4330499', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'PROJEC_CONREF_HN4330499'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1109176), 'leonardo.longaray', 'Soldagem de reparo aço carbono', 'Hora normal', '2539001', 'SOLDA_CARBO2_HN2539001', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1096764), 'bruno.fernandes', 'Ensaio por líquido penetrante (LP)', 'Hora normal', '7120100', 'ENSAIO_LIQPE3_HN7120100', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1045464), 'mariana.souza', 'Soldagem de revestimento por overlay', 'Adicional insalubridade (hora)', '2539001', 'SOLDA_OVERLA_INSAL2539001', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 1045374), 'mariana.souza', 'Soldagem de revestimento por overlay', 'Adicional insalubridade (hora)', '2539001', 'SOLDA_OVERLA_INSAL2539001', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'SOLDA_OVERLA_INSAL2539001'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 943501), 'carlos.menezes', 'Locação de guindaste hidráulico 80 t', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_GUIN80_OBS7732201', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 943381), 'carlos.menezes', 'Locação de guindaste hidráulico 80 t', 'Obs.: Itens que não são mão de obra', '7732201', 'LOCAC_GUIN80_OBS7732201', 'CADASTRADO', 'CONTESTOU_BLOQUEIO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_GUIN80_OBS7732201'), 'Guindaste de 80 t para içar o rotor do ventilador; o de 50 t não alcança o raio da área.'),
    (CURRENT_TIMESTAMP - make_interval(secs => 874371), 'thiago.almeida', 'Manutenção de forno elétrico a arco', 'Hora normal', '3314718', 'MANUT_FORNEA_HN3314718', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_FORNEA_HN3314718'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 838259), 'carlos.menezes', 'Execução de bases de equipamentos em concreto', 'Hora normal', '4399103', 'BASEEQ_EQUIPA_HN4399103', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'BASEEQ_EQUIPA_HN4399103'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 810991), 'carlos.menezes', 'Impermeabilização de laje de cobertura', 'Hora normal', '4330401', 'IMPERM_LAJEC2_HN4330401', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 659080), 'bruno.fernandes', 'Montagem de silos metálicos de 200 m³', 'Hora normal', '4292801', 'MONT_SILOS2_HN4292801', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 658960), 'bruno.fernandes', 'Montagem de silos metálicos de 200 m³', 'Hora normal', '4292801', 'MONT_SILOS2_HN4292801', 'CADASTRADO', 'CONTESTOU_BLOQUEIO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MONT_SILOS2_HN4292801'), 'Silo de 200 m³ para cal, com projeto estrutural próprio; não é o mesmo de 100 m³.'),
    (CURRENT_TIMESTAMP - make_interval(secs => 589873), 'comprador', 'Reparo de pavimento intertravado', 'Hora normal', '4211101', 'REPAR_PAVINT_HN4211101', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'REPAR_PAVINT_HN4211101'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 547349), 'juliana.prado', 'Hidrojateamento de equipamento', 'Hora normal', '8129000', 'HIDROJ_EQUIP2_HN8129000', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 478057), 'fernanda.lopes', 'Usinagem de peças de reposição', 'Hora normal', '2539001', 'USINAG_PECASR_HN2539001', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'USINAG_PECASR_HN2539001'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 471890), 'fernanda.lopes', 'Manutenção de caldeira de recuperação', 'Hora normal', '3311200', 'MANUT_CALDEI_HN3311200', 'CADASTRADO', 'CADASTROU_NOVO', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_CALDEI_HN3311200'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 438196), 'juliana.prado', 'Limpeza de tanques', 'Hora normal', '8129000', 'LIMP_TANRES_HN8129000', 'SUGERIDO', 'REUTILIZOU_EXISTENTE', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LIMP_TANRES_HN8129000'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 423048), 'mariana.souza', 'Locação caminhão munck', 'Obs.: Itens que não são mão de obra', '7719599', 'LOCAC_MUNCK2_OBS7719599', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 360058), 'thiago.almeida', 'Balanceamento dinâmico de ventiladores', 'Hora extra noturna Seg a Sab', '3314710', 'BALAN_VENTIL_HENS3314710', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 359968), 'thiago.almeida', 'Balanceamento dinâmico de ventiladores', 'Hora extra noturna Seg a Sab', '3314710', 'BALAN_VENTIL_HENS3314710', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'BALAN_VENTIL_HENS3314710'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 323865), 'bruno.fernandes', 'Manutenção de válvula gaveta de panela', 'Hora extra noturna Seg a Sab', '3314703', 'MANUT_GAVETA_HENS3314703', 'REQUER_CONFIRMACAO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 323775), 'bruno.fernandes', 'Manutenção de válvula gaveta de panela', 'Hora extra noturna Seg a Sab', '3314703', 'MANUT_GAVETA_HENS3314703', 'CADASTRADO', 'CONFIRMOU_MESMO_ASSIM', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_GAVETA_HENS3314703'), NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 221772), 'comprador', 'Calibração de instrumentos de pressão/temperatura', 'Hora normal', '3312102', 'CALIB_INSTR2_HN3312102', 'BLOQUEADO', NULL, NULL, NULL),
    (CURRENT_TIMESTAMP - make_interval(secs => 101308), 'fernanda.lopes', 'Montagem de andaimes fachadeiro', 'Hora normal', '4399102', 'ANDAIM_FACHA2_HN4399102', 'BLOQUEADO', NULL, NULL, NULL);

-- Sugestões mostradas em cada consulta (a similaridade é calculada com o mesmo operador do cadastro)
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Soldagem de revestimento por overlay'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1045464) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'SOLDA_OVERLA_INSAL2539001'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'SOLDA_OVERLA_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Soldagem de revestimento por overlay'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1045374) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'SOLDA_OVERLA_INSAL2539001'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'SOLDA_OVERLA_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de estrutura metálica de galpão'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2252797) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'MONT_GALPAO_HES4292801'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'MONT_GALPAO_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de estrutura metálica de galpão'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2252707) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'MONT_GALPAO_HES4292801'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'MONT_GALPAO_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Balanceamento dinâmico de ventiladores'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 360058) AND cc.usuario = 'thiago.almeida' AND cc.codigo_gerado = 'BALAN_VENTIL_HENS3314710'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'BALAN_VENTIL_HN3314710';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Balanceamento dinâmico de ventiladores'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 359968) AND cc.usuario = 'thiago.almeida' AND cc.codigo_gerado = 'BALAN_VENTIL_HENS3314710'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'BALAN_VENTIL_HN3314710';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção de válvula gaveta de panela'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 323865) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MANUT_GAVETA_HENS3314703'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'MANUT_GAVETA_HN3314703';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção de válvula gaveta de panela'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 323775) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MANUT_GAVETA_HENS3314703'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'MANUT_GAVETA_HN3314703';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Aspiração industrial de poeira'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1515024) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'ASPIRA_POEIRA_INSAL8129000'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'ASPIRA_POEIRA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Aspiração industrial de poeira'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1514934) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'ASPIRA_POEIRA_INSAL8129000'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'ASPIRA_POEIRA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Revestimento de chapas de desgaste'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2204882) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'REVEST_DESGAS_INSAL2539001'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'REVEST_DESGAS_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Revestimento de chapas de desgaste'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2204792) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'REVEST_DESGAS_INSAL2539001'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'REVEST_DESGAS_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção do redutor de velocidade'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3005316) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'MANUT_REDUT3_HN3314705'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MANUT_REDUTO_HN3314705';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção do redutor de velocidade'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3005316) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'MANUT_REDUT3_HN3314705'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MAN_REDVEL_HN3314705';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção do redutor de velocidade'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3005316) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'MANUT_REDUT3_HN3314705'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MANUT_REDUTR_HN3314705';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Limpeza industrial de laminadores'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3093124) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LIMP_LAMIN2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LIMPIND_LAMINA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Limpeza industrial de laminadores'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3093124) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LIMP_LAMIN2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LIMP_LAMINA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Limpeza industrial de laminadores'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3093124) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LIMP_LAMIN2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LIMP_LAMIND_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Hidrojateamento de equipamento'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 547349) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'HIDROJ_EQUIP2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'HIDROJ_EQUIPA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Hidrojateamento de equipamento'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 547349) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'HIDROJ_EQUIP2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'JATEAM_EQUIPA_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Hidrojateamento de equipamento'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 547349) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'HIDROJ_EQUIP2_HN8129000'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'HIDRO_EQUIP_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Soldagem de reparo aço carbono'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1109176) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'SOLDA_CARBO2_HN2539001'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'SOLDA_CARBON_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Soldagem de reparo aço carbono'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1109176) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'SOLDA_CARBO2_HN2539001'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'SOLD_ACOCAR_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Soldagem de reparo aço carbono'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1109176) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'SOLDA_CARBO2_HN2539001'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'SOLDA_REPCAR_HN2539001';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação caminhão munck'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 423048) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'LOCAC_MUNCK2_OBS7719599'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOCAC_MUNCK_OBS7719599';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação caminhão munck'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 423048) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'LOCAC_MUNCK2_OBS7719599'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOC_MUNCK_OBS7719599';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação caminhão munck'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 423048) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'LOCAC_MUNCK2_OBS7719599'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ALUG_MUNCK_OBS7719599';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Calibração de instrumentos de pressão/temperatura'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 221772) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'CALIB_INSTR2_HN3312102'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'CALIB_INSTRU_HN3312102';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Calibração de instrumentos de pressão/temperatura'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 221772) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'CALIB_INSTR2_HN3312102'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'CALIB_PRETEM_HN3312102';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Ensaio por líquido penetrante (LP)'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1096764) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'ENSAIO_LIQPE3_HN7120100'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ENSAIO_LIQPEN_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Ensaio por líquido penetrante (LP)'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1096764) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'ENSAIO_LIQPE3_HN7120100'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ENSAIO_LP_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Ensaio por líquido penetrante (LP)'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1096764) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'ENSAIO_LIQPE3_HN7120100'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ENS_LIQPE2_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Instalação de eletrocalhas'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1367755) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'INSTAL_CALHA2_HN4321500'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'INSTAL_CALHAS_HN4321500';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Instalação de eletrocalhas'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1367755) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'INSTAL_CALHA2_HN4321500'
  AND cc.resultado = 'REQUER_CONFIRMACAO' AND s.codigo_padronizado = 'INST_ELCALH_HN4321500';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de andaimes fachadeiro'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 101308) AND cc.usuario = 'fernanda.lopes' AND cc.codigo_gerado = 'ANDAIM_FACHA2_HN4399102'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ANDAIM_FACHAD_HN4399102';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de andaimes fachadeiro'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 101308) AND cc.usuario = 'fernanda.lopes' AND cc.codigo_gerado = 'ANDAIM_FACHA2_HN4399102'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MONTAND_FACHAD_HN4399102';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Impermeabilização de laje de cobertura'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 810991) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'IMPERM_LAJEC2_HN4330401'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'IMPERM_LAJECO_HN4330401';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Impermeabilização de laje de cobertura'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 810991) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'IMPERM_LAJEC2_HN4330401'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'IMPER_LAJCOB_HN4330401';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção de compressor de ar comprimido'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3282996) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'MANUT_COMPR2_HN3314704'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MANUT_COMPRE_HN3314704';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção de compressor de ar comprimido'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3282996) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'MANUT_COMPR2_HN3314704'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MANUT_COMPRS_HN3314704';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção de compressor de ar comprimido'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3282996) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'MANUT_COMPR2_HN3314704'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MCOMP_AR_HN3314704';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Troca de eletrodo de grafite do forno'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1925870) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'TROCA_ELETR2_HN3314718'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'TROCA_ELETRO_HN3314718';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Troca de eletrodo de grafite do forno'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1925870) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'TROCA_ELETR2_HN3314718'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'TROCA_GRAFIT_HN3314718';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Análise de vibração em equipamentos rotativos'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1523785) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'ANAVIB_ROTAT2_HN7120100'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'ANAVIB_ROTATI_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Análise de vibração em equipamentos rotativos'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1523785) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'ANAVIB_ROTAT2_HN7120100'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'VIBRAC_ROTAT_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Pintura anticorrosiva de estrutura metálica'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2583699) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'PINT_ESTME2_HN4330404'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'PINT_ESTMET_HN4330404';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Pintura anticorrosiva de estrutura metálica'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2583699) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'PINT_ESTME2_HN4330404'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'PINTAC_ESTMT_HN4330404';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Pintura anticorrosiva de estrutura metálica'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2583699) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'PINT_ESTME2_HN4330404'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'PINT_ANTICO_HN4330404';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Limpeza de tanques'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 438196) AND cc.usuario = 'juliana.prado' AND cc.codigo_gerado = 'LIMP_TANRES_HN8129000'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'LIMP_TANRES_HN8129000';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Inspeção por ultrassom'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 1936323) AND cc.usuario = 'comprador' AND cc.codigo_gerado = 'INSP_ULTRAS_HN7120100'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'INSP_ULTRAS_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Linha de ar comprimido'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3047128) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'INSTAL_ARCOMP_HN3329599'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'INSTAL_ARCOMP_HN3329599';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção da ponte rolante'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3381071) AND cc.usuario = 'thiago.almeida' AND cc.codigo_gerado = 'MANUT_PONTER_HN3314710'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'MANUT_PONTER_HN3314710';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Teste hidrostático'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2078018) AND cc.usuario = 'rafael.teixeira' AND cc.codigo_gerado = 'TESTEH_HIDROS_HN7120100'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'TESTEH_HIDROS_HN7120100';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Munck para içamento'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 3418943) AND cc.usuario = 'mariana.souza' AND cc.codigo_gerado = 'LOCAC_MUNCK_OBS7719599'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'LOCAC_MUNCK_OBS7719599';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Manutenção em trocadores de calor'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2549387) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'MANUT_TROCAD_HN3314706'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'MANUT_TROCAD_HN3314706';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Aterramento de equipamentos'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2613837) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'INSTAL_ATERRA_HN4321500'
  AND cc.resultado = 'SUGERIDO' AND s.codigo_padronizado = 'INSTAL_ATERRA_HN4321500';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de guindaste hidráulico 80 t'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 943501) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'LOCAC_GUIN80_OBS7732201'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOCAC_GUINDA_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de guindaste hidráulico 80 t'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 943501) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'LOCAC_GUIN80_OBS7732201'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOCAC_PLATEL_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de guindaste hidráulico 80 t'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 943381) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'LOCAC_GUIN80_OBS7732201'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'LOCAC_GUINDA_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de guindaste hidráulico 80 t'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 943381) AND cc.usuario = 'carlos.menezes' AND cc.codigo_gerado = 'LOCAC_GUIN80_OBS7732201'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'LOCAC_PLATEL_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de silos metálicos de 200 m³'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 659080) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MONT_SILOS2_HN4292801'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MONT_SILOS_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de silos metálicos de 200 m³'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 659080) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MONT_SILOS2_HN4292801'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'MONT_GALPAO_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de silos metálicos de 200 m³'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 658960) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MONT_SILOS2_HN4292801'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'MONT_SILOS_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Montagem de silos metálicos de 200 m³'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 658960) AND cc.usuario = 'bruno.fernandes' AND cc.codigo_gerado = 'MONT_SILOS2_HN4292801'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'MONT_GALPAO_HN4292801';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de plataforma elevatória 20 m'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2262343) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LOCAC_PLATE2_OBS7732201'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOCAC_PLATEL_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de plataforma elevatória 20 m'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2262343) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LOCAC_PLATE2_OBS7732201'
  AND cc.resultado = 'BLOQUEADO' AND s.codigo_padronizado = 'LOCAC_GUINDA_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de plataforma elevatória 20 m'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2262223) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LOCAC_PLATE2_OBS7732201'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'LOCAC_PLATEL_OBS7732201';
INSERT INTO consulta_cadastro_sugestoes (consulta_id, servico_id, codigo_padronizado, texto_breve, score)
SELECT cc.id, s.id_servico, s.codigo_padronizado, s.texto_breve, similarity(lower('Locação de plataforma elevatória 20 m'), lower(s.texto_breve))
FROM consultas_cadastro cc, servicos_mock s
WHERE cc.criado_em = CURRENT_TIMESTAMP - make_interval(secs => 2262223) AND cc.usuario = 'leonardo.longaray' AND cc.codigo_gerado = 'LOCAC_PLATE2_OBS7732201'
  AND cc.resultado = 'CADASTRADO' AND s.codigo_padronizado = 'LOCAC_GUINDA_OBS7732201';

-- Dicionário De-Para (aprovados valem como busca exata; pendentes aguardam o aprovador)
INSERT INTO dicionario_de_para (nome_normalizado, nome_original, codigo_nbs, origem, score, status, confirmacoes, criado_em, revisado_por, revisado_em, motivo)
VALUES
    ('manutencao de forno eletrico a arco', 'Manutenção de forno elétrico a arco', '1.2001.50.00', 'LOTE', 0.71, 'APROVADO', 4, CURRENT_TIMESTAMP - make_interval(secs => 3459600), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2592000), NULL),
    ('limpeza industrial de laminador', 'Limpeza industrial de laminador', '1.1803.29.00', 'USUARIO', 0.58, 'APROVADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 3373200), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2505600), NULL),
    ('hidrojateamento de equipamentos', 'Hidrojateamento de equipamentos', '1.1803.29.00', 'USUARIO', 0.55, 'APROVADO', 5, CURRENT_TIMESTAMP - make_interval(secs => 3286800), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2419200), NULL),
    ('montagem de estrutura metalica', 'Montagem de estrutura metálica', '1.0105.50.00', 'LOTE', 0.83, 'APROVADO', 6, CURRENT_TIMESTAMP - make_interval(secs => 3200400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2332800), NULL),
    ('pintura anticorrosiva de estruturas', 'Pintura anticorrosiva de estruturas', '1.0107.30.00', 'LOTE', 0.64, 'APROVADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 3114000), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2246400), NULL),
    ('locacao de guindaste', 'Locação de guindaste', '1.1101.30.00', 'USUARIO', 0.61, 'APROVADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 3027600), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2160000), NULL),
    ('locacao de caminhao munck', 'Locação de caminhão munck', '1.1101.12.00', 'USUARIO', 0.52, 'APROVADO', 4, CURRENT_TIMESTAMP - make_interval(secs => 2941200), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 2073600), NULL),
    ('inspecao por ultrassom', 'Inspeção por ultrassom', '1.1404.49.00', 'LOTE', 0.47, 'APROVADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 2854800), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1987200), NULL),
    ('termografia de paineis eletricos', 'Termografia de painéis elétricos', '1.1404.43.00', 'USUARIO', 0.49, 'APROVADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 2768400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1900800), NULL),
    ('calibracao de instrumentos', 'Calibração de instrumentos', '1.1404.49.00', 'USUARIO', 0.44, 'APROVADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 2682000), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1814400), NULL),
    ('demolicao de estrutura de concreto', 'Demolição de estrutura de concreto', '1.0103.10.00', 'LOTE', 0.66, 'APROVADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 2595600), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1728000), NULL),
    ('impermeabilizacao de lajes', 'Impermeabilização de lajes', '1.0105.30.00', 'LOTE', 0.72, 'APROVADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 2509200), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1641600), NULL),
    ('mao de obra temporaria para parada', 'Mão de obra temporária para parada', '1.1801.22.00', 'USUARIO', 0.57, 'APROVADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 2422800), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 1555200), NULL),
    ('troca de eletrodos de grafite', 'Troca de eletrodos de grafite', '1.2001.50.00', 'USUARIO', 0.46, 'PENDENTE', 1, CURRENT_TIMESTAMP - make_interval(secs => 2336400), NULL, NULL, NULL),
    ('reparo de revestimento refratario de panela', 'Reparo de revestimento refratário de panela', '1.2001.50.00', 'USUARIO', 0.41, 'PENDENTE', 2, CURRENT_TIMESTAMP - make_interval(secs => 2250000), NULL, NULL, NULL),
    ('balanceamento de ventiladores', 'Balanceamento de ventiladores', '1.2001.50.00', 'LOTE', 0.53, 'PENDENTE', 3, CURRENT_TIMESTAMP - make_interval(secs => 2163600), NULL, NULL, NULL),
    ('alinhamento a laser de acionamentos', 'Alinhamento a laser de acionamentos', '1.2001.50.00', 'USUARIO', 0.5, 'PENDENTE', 1, CURRENT_TIMESTAMP - make_interval(secs => 2077200), NULL, NULL, NULL),
    ('manutencao de ponte rolante', 'Manutenção de ponte rolante', '1.2001.50.00', 'USUARIO', 0.62, 'PENDENTE', 5, CURRENT_TIMESTAMP - make_interval(secs => 1990800), NULL, NULL, NULL),
    ('montagem de andaime fachadeiro', 'Montagem de andaime fachadeiro', '1.0105.70.00', 'LOTE', 0.78, 'PENDENTE', 2, CURRENT_TIMESTAMP - make_interval(secs => 1904400), NULL, NULL, NULL),
    ('lancamento de cabos de forca', 'Lançamento de cabos de força', '1.0106.11.00', 'USUARIO', 0.6, 'PENDENTE', 1, CURRENT_TIMESTAMP - make_interval(secs => 1818000), NULL, NULL, NULL),
    ('recuperacao de piso industrial', 'Recuperação de piso industrial', '1.0107.40.00', 'LOTE', 0.45, 'PENDENTE', 4, CURRENT_TIMESTAMP - make_interval(secs => 1731600), NULL, NULL, NULL),
    ('servico geral', 'Serviço geral', '1.0105.90.00', 'USUARIO', 0.22, 'REJEITADO', 1, CURRENT_TIMESTAMP - make_interval(secs => 1645200), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 777600), 'Nome genérico demais para mapear.'),
    ('mao de obra', 'Mão de obra', '1.1801.29.00', 'USUARIO', 0.25, 'REJEITADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 1558800), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 691200), 'Nome genérico demais para mapear.'),
    ('manutencao', 'Manutenção', '1.2001.50.00', 'LOTE', 0.3, 'REJEITADO', 3, CURRENT_TIMESTAMP - make_interval(secs => 1472400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 604800), 'Sem o equipamento no nome, não dá para escolher o NBS.'),
    ('locacao de equipamento', 'Locação de equipamento', '1.1101.90.00', 'USUARIO', 0.35, 'REVOGADO', 2, CURRENT_TIMESTAMP - make_interval(secs => 1386000), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518400), 'Mapeamento amplo demais; substituído por mapeamentos específicos.');

-- Trilha de auditoria do De-Para
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3459600) FROM dicionario_de_para WHERE nome_normalizado = 'manutencao de forno eletrico a arco' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2592000) FROM dicionario_de_para WHERE nome_normalizado = 'manutencao de forno eletrico a arco' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3373200) FROM dicionario_de_para WHERE nome_normalizado = 'limpeza industrial de laminador' AND codigo_nbs = '1.1803.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2505600) FROM dicionario_de_para WHERE nome_normalizado = 'limpeza industrial de laminador' AND codigo_nbs = '1.1803.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3286800) FROM dicionario_de_para WHERE nome_normalizado = 'hidrojateamento de equipamentos' AND codigo_nbs = '1.1803.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2419200) FROM dicionario_de_para WHERE nome_normalizado = 'hidrojateamento de equipamentos' AND codigo_nbs = '1.1803.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3200400) FROM dicionario_de_para WHERE nome_normalizado = 'montagem de estrutura metalica' AND codigo_nbs = '1.0105.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2332800) FROM dicionario_de_para WHERE nome_normalizado = 'montagem de estrutura metalica' AND codigo_nbs = '1.0105.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3114000) FROM dicionario_de_para WHERE nome_normalizado = 'pintura anticorrosiva de estruturas' AND codigo_nbs = '1.0107.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2246400) FROM dicionario_de_para WHERE nome_normalizado = 'pintura anticorrosiva de estruturas' AND codigo_nbs = '1.0107.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 3027600) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de guindaste' AND codigo_nbs = '1.1101.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2160000) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de guindaste' AND codigo_nbs = '1.1101.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2941200) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de caminhao munck' AND codigo_nbs = '1.1101.12.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2073600) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de caminhao munck' AND codigo_nbs = '1.1101.12.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2854800) FROM dicionario_de_para WHERE nome_normalizado = 'inspecao por ultrassom' AND codigo_nbs = '1.1404.49.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1987200) FROM dicionario_de_para WHERE nome_normalizado = 'inspecao por ultrassom' AND codigo_nbs = '1.1404.49.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2768400) FROM dicionario_de_para WHERE nome_normalizado = 'termografia de paineis eletricos' AND codigo_nbs = '1.1404.43.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1900800) FROM dicionario_de_para WHERE nome_normalizado = 'termografia de paineis eletricos' AND codigo_nbs = '1.1404.43.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2682000) FROM dicionario_de_para WHERE nome_normalizado = 'calibracao de instrumentos' AND codigo_nbs = '1.1404.49.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1814400) FROM dicionario_de_para WHERE nome_normalizado = 'calibracao de instrumentos' AND codigo_nbs = '1.1404.49.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2595600) FROM dicionario_de_para WHERE nome_normalizado = 'demolicao de estrutura de concreto' AND codigo_nbs = '1.0103.10.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1728000) FROM dicionario_de_para WHERE nome_normalizado = 'demolicao de estrutura de concreto' AND codigo_nbs = '1.0103.10.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2509200) FROM dicionario_de_para WHERE nome_normalizado = 'impermeabilizacao de lajes' AND codigo_nbs = '1.0105.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1641600) FROM dicionario_de_para WHERE nome_normalizado = 'impermeabilizacao de lajes' AND codigo_nbs = '1.0105.30.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2422800) FROM dicionario_de_para WHERE nome_normalizado = 'mao de obra temporaria para parada' AND codigo_nbs = '1.1801.22.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1555200) FROM dicionario_de_para WHERE nome_normalizado = 'mao de obra temporaria para parada' AND codigo_nbs = '1.1801.22.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2336400) FROM dicionario_de_para WHERE nome_normalizado = 'troca de eletrodos de grafite' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2250000) FROM dicionario_de_para WHERE nome_normalizado = 'reparo de revestimento refratario de panela' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2163600) FROM dicionario_de_para WHERE nome_normalizado = 'balanceamento de ventiladores' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 2077200) FROM dicionario_de_para WHERE nome_normalizado = 'alinhamento a laser de acionamentos' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1990800) FROM dicionario_de_para WHERE nome_normalizado = 'manutencao de ponte rolante' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1904400) FROM dicionario_de_para WHERE nome_normalizado = 'montagem de andaime fachadeiro' AND codigo_nbs = '1.0105.70.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1818000) FROM dicionario_de_para WHERE nome_normalizado = 'lancamento de cabos de forca' AND codigo_nbs = '1.0106.11.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1731600) FROM dicionario_de_para WHERE nome_normalizado = 'recuperacao de piso industrial' AND codigo_nbs = '1.0107.40.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1645200) FROM dicionario_de_para WHERE nome_normalizado = 'servico geral' AND codigo_nbs = '1.0105.90.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'REJEITADO', 'aprovador', 'Nome genérico demais para mapear.', CURRENT_TIMESTAMP - make_interval(secs => 777600) FROM dicionario_de_para WHERE nome_normalizado = 'servico geral' AND codigo_nbs = '1.0105.90.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1558800) FROM dicionario_de_para WHERE nome_normalizado = 'mao de obra' AND codigo_nbs = '1.1801.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'REJEITADO', 'aprovador', 'Nome genérico demais para mapear.', CURRENT_TIMESTAMP - make_interval(secs => 691200) FROM dicionario_de_para WHERE nome_normalizado = 'mao de obra' AND codigo_nbs = '1.1801.29.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'importacao', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1472400) FROM dicionario_de_para WHERE nome_normalizado = 'manutencao' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'REJEITADO', 'aprovador', 'Sem o equipamento no nome, não dá para escolher o NBS.', CURRENT_TIMESTAMP - make_interval(secs => 604800) FROM dicionario_de_para WHERE nome_normalizado = 'manutencao' AND codigo_nbs = '1.2001.50.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, NULL, 'PENDENTE', 'comprador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 1386000) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de equipamento' AND codigo_nbs = '1.1101.90.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'PENDENTE', 'APROVADO', 'aprovador', NULL, CURRENT_TIMESTAMP - make_interval(secs => 522000) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de equipamento' AND codigo_nbs = '1.1101.90.00';
INSERT INTO dicionario_de_para_historico (de_para_id, de_status, para_status, usuario, motivo, em)
SELECT id, 'APROVADO', 'REVOGADO', 'aprovador', 'Mapeamento amplo demais; substituído por mapeamentos específicos.', CURRENT_TIMESTAMP - make_interval(secs => 518400) FROM dicionario_de_para WHERE nome_normalizado = 'locacao de equipamento' AND codigo_nbs = '1.1101.90.00';

-- Lotes de importação
INSERT INTO lotes_importacao (arquivo_nome, enviado_por, criado_em, status, total_linhas, processadas)
VALUES ('manutencao_mecanica_outubro.xlsx', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 778200), 'CONCLUIDO', 18, 18);
INSERT INTO lotes_importacao (arquivo_nome, enviado_por, criado_em, status, total_linhas, processadas)
VALUES ('paradas_refratarios_acimaria.csv', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173400), 'CONCLUIDO', 12, 12);
INSERT INTO lotes_importacao (arquivo_nome, enviado_por, criado_em, status, total_linhas, processadas)
VALUES ('automacao_novos_servicos.csv', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 600), 'CONCLUIDO', 12, 12);

-- Linhas dos lotes (a similaridade das parecidas é calculada contra o serviço parecido)
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 1, 'Eletromecânica', 'Instalação de sistema de exaustão em cabine de pintura', 'INSTAL', '_EXAUST', 'Hora normal', '4322302', '1.0106.32.00', 'comprador',
    'INSTAL_EXAUST_HN4322302', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_EXAUST_HN4322302'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 2, 'Eletromecânica', 'Manutenção de monovias e talhas elétricas', 'MANUT', '_MONOVI', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'MANUT_MONOVI_HN3314710', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_MONOVI_HN3314710'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 3, 'Eletromecânica', 'Substituição de rolamentos de mesa de rolos', 'SUBST', '_ROLAMI', 'Hora extra Seg a Sab', '3314705', '1.2001.50.00', 'comprador',
    'SUBST_ROLAMI_HES3314705', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'SUBST_ROLAMI_HES3314705'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 4, 'Caldeiraria', 'Fabricação de grades de piso metálicas', 'FABRIC', '_GRADES', 'Hora normal', '2511000', '1.2001.10.00', 'comprador',
    'FABRIC_GRADES_HN2511000', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'FABRIC_GRADES_HN2511000'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 5, 'Civil', 'Recuperação de sistema de drenagem pluvial', 'RECUP', '_DRENAG', 'Hora normal', '4399199', '1.0105.90.00', 'comprador',
    'RECUP_DRENAG_HN4399199', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'RECUP_DRENAG_HN4399199'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 6, 'Civil', 'Demarcação de áreas e faixas de segurança', 'DEMARC', '_FAIXAS', 'Hora normal', '4330404', '1.0107.30.00', 'comprador',
    'DEMARC_FAIXAS_HN4330404', 'NOVA', 'APROVADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'DEMARC_FAIXAS_HN4330404'));
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 7, 'Eletromecânica', 'Manutenção de monovias e talhas elétricas', 'MANUT', '_TALHAS', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'MANUT_TALHAS_HN3314710', 'NOVA', 'CONFLITO', NULL, NULL, 'Parecido com MANUT_MONOVI_HN3314710 (similaridade 100%) já na base; verifique se é duplicado', NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 8, 'Limpeza industrial', 'Jateamento hidráulico de equipamentos', 'JATEHI', '_EQUIPA', 'Hora normal', '8129000', '1.1803.29.00', 'comprador',
    'JATEHI_EQUIPA_HN8129000', 'PARECIDA', 'APROVADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'HIDROJ_EQUIPA_HN8129000'), (SELECT similarity(lower('Jateamento hidráulico de equipamentos'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'HIDROJ_EQUIPA_HN8129000'), NULL, 'MESMO_SERVICO', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 9, 'Automação', 'Calibração de instrumentos de medição de pressão e temperatura', 'CALIBR', '_MEDICA', 'Hora normal', '3312102', '1.1404.49.00', 'comprador',
    'CALIBR_MEDICA_HN3312102', 'PARECIDA', 'APROVADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'CALIB_INSTRU_HN3312102'), (SELECT similarity(lower('Calibração de instrumentos de medição de pressão e temperatura'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'CALIB_INSTRU_HN3312102'), NULL, 'MESMO_SERVICO', 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 10, 'Eletromecânica', 'Serviços diversos de manutenção', 'MANUT', '_DIVERS', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'MANUT_DIVERS_HN3314710', 'NOVA', 'REJEITADA', NULL, NULL, 'Descrição genérica: informe o equipamento e a atividade.', NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 11, 'Eletromecânica', 'Mão de obra para obra', 'MOBRA', '_OBRA', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'MOBRA_OBRA_HN3314710', 'NOVA', 'REJEITADA', NULL, NULL, 'Descrição genérica: informe a atividade e a área.', NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), CURRENT_TIMESTAMP - make_interval(secs => 691400), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 12, 'Caldeiraria', 'Soldagem de reparo em aço carbono', 'SOLDA', '_CARBON', 'Hora normal', '2539001', '1.2001.10.00', 'comprador',
    'SOLDA_CARBON_HN2539001', 'EXISTENTE', 'IGNORADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'SOLDA_CARBON_HN2539001'), (SELECT similarity(lower('Soldagem de reparo em aço carbono'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'SOLDA_CARBON_HN2539001'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 13, 'Inspeção e ensaios', 'Inspeção por ultrassom em soldas', 'INSP', '_ULTRAS', 'Hora normal', '7120100', '1.1404.49.00', 'comprador',
    'INSP_ULTRAS_HN7120100', 'EXISTENTE', 'IGNORADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSP_ULTRAS_HN7120100'), (SELECT similarity(lower('Inspeção por ultrassom em soldas'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'INSP_ULTRAS_HN7120100'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 14, 'Eletromecânica', 'Substituição de rolamentos de mesa de rolos', 'SUBST', '_ROLAMI', 'Hora extra Seg a Sab', '3314705', '1.2001.50.00', 'comprador',
    'SUBST_ROLAMI_HES3314705', 'REPETIDA', 'IGNORADA', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 15, 'Eletromecânica', 'Serviço de teste', 'TESTE', '_TESTE', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'TESTE_TESTE_HN3314710', 'NOVA', 'DESCARTADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 691500), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 16, 'Eletromecânica', 'Manutenção de sistema de lubrificação centralizada', 'MANUT', '_LUBCEN', 'Hora normal', '3314710', '1.2001.50.00', 'comprador',
    'MANUT_LUBCEN_HN3314710', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 86700), CURRENT_TIMESTAMP - make_interval(secs => 86600), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 17, 'Caldeiraria', 'Montagem de plataformas e passarelas metálicas', 'MONT', '_PLATAF', 'Hora normal', '4292801', '1.0105.50.00', 'comprador',
    'MONT_PLATAF_HN4292801', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 86700), CURRENT_TIMESTAMP - make_interval(secs => 86600), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'manutencao_mecanica_outubro.xlsx'), 18, 'Limpeza industrial', 'Limpeza de dutos de ventilação', 'LIMP', '_DUTOSV', 'Hora normal', '8129000', '1.1803.29.00', 'comprador',
    'LIMP_DUTOSV_HN8129000', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 86700), CURRENT_TIMESTAMP - make_interval(secs => 86600), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 1, 'Refratários', 'Reparo de abóbada refrigerada do forno', 'REPAR', '_ABOBAD', 'Hora normal', '4330499', '1.2001.50.00', 'mariana.souza',
    'REPAR_ABOBAD_HN4330499', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 2, 'Refratários', 'Substituição de calha de vazamento do forno', 'SUBST', '_CALHAV', 'Hora normal', '4330499', '1.2001.50.00', 'mariana.souza',
    'SUBST_CALHAV_HN4330499', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 3, 'Refratários', 'Substituição de bocais de panela', 'SUBST', '_BOCAIS', 'Hora normal', '4330499', '1.2001.50.00', 'mariana.souza',
    'SUBST_BOCAIS_HN4330499', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 4, 'Refratários', 'Colagem de massa refratária em canal de corrida', 'COLAGE', '_CANAL', 'Hora extra Seg a Sab', '4330499', '1.2001.50.00', 'mariana.souza',
    'COLAGE_CANAL_HES4330499', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 5, 'Inspeção e ensaios', 'Inspeção termográfica de revestimento de panela', 'INSTER', '_TERMOP', 'Hora normal', '7120100', '1.1404.43.00', 'mariana.souza',
    'INSTER_TERMOP_HN7120100', 'NOVA', 'ENVIADA', NULL, NULL, NULL, NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 6, 'Civil', 'Montagem de andaimes internos de forno', 'ANDAIM', '_INTFOR', 'Hora normal', '4399102', '1.0105.70.00', 'mariana.souza',
    'ANDAIM_INTFOR_HN4399102', 'PARECIDA', 'ENVIADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'ANDAIM_FACHAD_HN4399102'), (SELECT similarity(lower('Montagem de andaimes internos de forno'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'ANDAIM_FACHAD_HN4399102'), NULL, 'OUTRO_SERVICO', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 7, 'Refratários', 'Reparo de revestimento refratário de panela de aço na região de escória', 'REFRAT', '_ESCORI', 'Hora normal', '4330499', '1.2001.50.00', 'mariana.souza',
    'REFRAT_ESCORI_HN4330499', 'PARECIDA', 'ENVIADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'REFRAT_PANELA_HN4330499'), (SELECT similarity(lower('Reparo de revestimento refratário de panela de aço na região de escória'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'REFRAT_PANELA_HN4330499'), NULL, 'OUTRO_SERVICO', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 8, 'Limpeza industrial', 'Limpeza de tanques e reservatórios industriais', 'LIMP', '_TANIND', 'Hora normal', '8129000', '1.1803.29.00', 'mariana.souza',
    'LIMP_TANIND_HN8129000', 'PARECIDA', 'ENVIADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LIMP_TANRES_HN8129000'), (SELECT similarity(lower('Limpeza de tanques e reservatórios industriais'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'LIMP_TANRES_HN8129000'), NULL, 'MESMO_SERVICO', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 9, 'Eletromecânica', 'Manutenção de redutores de velocidade industriais', 'MANUT', '_REDIND', 'Hora normal', '3314705', '1.2001.50.00', 'mariana.souza',
    'MANUT_REDIND_HN3314705', 'PARECIDA', 'ENVIADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_REDUTO_HN3314705'), (SELECT similarity(lower('Manutenção de redutores de velocidade industriais'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'MANUT_REDUTO_HN3314705'), NULL, 'MESMO_SERVICO', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 10, 'Apoio e logística', 'Locação de guindaste hidráulico de 50 toneladas', 'LOCAC', '_GUIT50', 'Obs.: Itens que não são mão de obra', '7732201', '1.1101.30.00', 'mariana.souza',
    'LOCAC_GUIT50_OBS7732201', 'PARECIDA', 'ENVIADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_GUINDA_OBS7732201'), (SELECT similarity(lower('Locação de guindaste hidráulico de 50 toneladas'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'LOCAC_GUINDA_OBS7732201'), NULL, 'MESMO_SERVICO', 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 11, 'Caldeiraria', 'Soldagem de reparo em aço carbono (chapas de desgaste)', 'SOLDA', '_CARBON', 'Hora normal', '2539001', '1.2001.10.00', 'mariana.souza',
    'SOLDA_CARBON_HN2539001', 'NOVA', 'CONFLITO', NULL, NULL, 'Código já cadastrado: SOLDA_CARBON_HN2539001', NULL, 'mariana.souza', CURRENT_TIMESTAMP - make_interval(secs => 173100), CURRENT_TIMESTAMP - make_interval(secs => 173000), 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 86500), NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'paradas_refratarios_acimaria.csv'), 12, 'Refratários', 'Reparo de revestimento refratário', 'REFRAT-1', '_GERAL', 'Hora normal', '433049', '1.2001.50.00', 'mariana.souza',
    NULL, 'ERRO', 'EM_CONFERENCIA', NULL, NULL, 'CNAE: deve ter 7 dígitos', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 1, 'Automação', 'Atualização de firmware de CLP', 'ATUFW', '_CLP', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'ATUFW_CLP_HN3321000', 'NOVA', 'PRONTA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 300), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 2, 'Automação', 'Instalação de medidor de vazão eletromagnético', 'INSTAL', '_VAZAO', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'INSTAL_VAZAO_HN3321000', 'NOVA', 'PRONTA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 300), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 3, 'Automação', 'Parametrização de drives de acionamento', 'PARAME', '_DRIVES', 'Hora normal', '6209100', '1.2001.50.00', 'comprador',
    'PARAME_DRIVES_HN6209100', 'NOVA', 'PRONTA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 300), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 4, 'Automação', 'Troca de IHM da sala de controle', 'TROCA', '_IHMSAL', 'Hora normal', '3313999', '1.2001.50.00', 'comprador',
    'TROCA_IHMSAL_HN3313999', 'NOVA', 'PRONTA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 300), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 5, 'Automação', 'Comissionamento de malhas de controle', 'COMISS', '_MALHAS', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'COMISS_MALHAS_HN3321000', 'NOVA', 'PRONTA', NULL, NULL, NULL, NULL, 'comprador', CURRENT_TIMESTAMP - make_interval(secs => 300), NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 6, 'Automação', 'Manutenção de inversor de frequência', 'MANUT', '_INVER2', 'Hora normal', '3313999', '1.2001.50.00', 'comprador',
    'MANUT_INVER2_HN3313999', 'PARECIDA', 'EM_CONFERENCIA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'MANUT_INVERS_HN3313999'), (SELECT similarity(lower('Manutenção de inversor de frequência'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'MANUT_INVERS_HN3313999'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 7, 'Automação', 'Programação de CLP', 'PROG', '_CLP', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'PROG_CLP_HN3321000', 'PARECIDA', 'EM_CONFERENCIA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'PROG_CLPIND_HN3321000'), (SELECT similarity(lower('Programação de CLP'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'PROG_CLPIND_HN3321000'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 8, 'Automação', 'Instalação de sensor de nível', 'INSTAL', '_NIVEL', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'INSTAL_NIVEL_HN3321000', 'PARECIDA', 'EM_CONFERENCIA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_SENSOR_HN3321000'), (SELECT similarity(lower('Instalação de sensor de nível'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_SENSOR_HN3321000'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 9, 'Automação', 'Instalação de instrumentação de campo', 'INSTAL', '_CAMPO', 'Hora normal', '3321000', '1.2003.21.90', 'comprador',
    'INSTAL_CAMPO_HN3321000', 'EXISTENTE', 'IGNORADA', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_CAMPO_HN3321000'), (SELECT similarity(lower('Instalação de instrumentação de campo'), lower(texto_breve)) FROM servicos_mock WHERE codigo_padronizado = 'INSTAL_CAMPO_HN3321000'), NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 10, 'Automação', 'Manutenção de rede de sensores', 'MANUT', '_SENSOR', 'Hora normal', '33140', '1.2001.50.00', 'comprador',
    NULL, 'ERRO', 'EM_CONFERENCIA', NULL, NULL, 'CNAE: deve ter 7 dígitos', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 11, 'Automação', 'Teste de intertravamentos de segurança', 'TESTE', '_INTERT', 'Hora normal 50%', '7120100', '1.1404.43.00', 'comprador',
    NULL, 'ERRO', 'EM_CONFERENCIA', NULL, NULL, 'Condição: não existe ou está inativa', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO linhas_importacao (lote_id, numero, area_servico, texto_breve, abreviatura, complemento, condicao, cnae, codigo_nbs, criador_codigo,
    codigo_gerado, situacao, etapa, servico_parecido_id, score, motivo, decisao, conferido_por, conferido_em, enviado_em, decidido_por, decidido_em, servico_criado_id)
VALUES ((SELECT id FROM lotes_importacao WHERE arquivo_nome = 'automacao_novos_servicos.csv'), 12, 'Automação', 'Manutenção de CLP de linha', 'MANUT-ELE', '_CLPLIN', 'Hora normal', '3313999', '1.2001.50.00', 'comprador',
    NULL, 'ERRO', 'EM_CONFERENCIA', NULL, NULL, 'Abreviatura: use apenas letras e números', NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- Sinônimos aprovados no lote A (o mesmo texto, condição e CNAE passam a apontar para o serviço já existente)
INSERT INTO servico_sinonimo (texto_normalizado, condicao, cnae, servico_id, texto_original, aprovado_por, aprovado_em, linha_id)
SELECT 'jateamento hidraulico de equipamentos', 'Hora normal', '8129000', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'HIDROJ_EQUIPA_HN8129000'), 'Jateamento hidráulico de equipamentos', 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500),
    (SELECT li.id FROM linhas_importacao li JOIN lotes_importacao lo ON lo.id = li.lote_id WHERE lo.arquivo_nome = 'manutencao_mecanica_outubro.xlsx' AND li.codigo_gerado = 'JATEHI_EQUIPA_HN8129000');
INSERT INTO servico_sinonimo (texto_normalizado, condicao, cnae, servico_id, texto_original, aprovado_por, aprovado_em, linha_id)
SELECT 'calibracao de instrumentos de medicao de pressao e temperatura', 'Hora normal', '3312102', (SELECT id_servico FROM servicos_mock WHERE codigo_padronizado = 'CALIB_INSTRU_HN3312102'), 'Calibração de instrumentos de medição de pressão e temperatura', 'aprovador', CURRENT_TIMESTAMP - make_interval(secs => 518500),
    (SELECT li.id FROM linhas_importacao li JOIN lotes_importacao lo ON lo.id = li.lote_id WHERE lo.arquivo_nome = 'manutencao_mecanica_outubro.xlsx' AND li.codigo_gerado = 'CALIBR_MEDICA_HN3312102');
