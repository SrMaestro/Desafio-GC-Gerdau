-- Login com perfis. As senhas dos dois usuários de exemplo estão em docs/GUIA_DE_USO.md; troque-as antes de usar de verdade.
CREATE TABLE usuarios (
    id         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil     VARCHAR(10)  NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT usuarios_username_key UNIQUE (username),
    CONSTRAINT usuarios_perfil_check CHECK (perfil IN ('COMPRADOR', 'APROVADOR'))
);

INSERT INTO usuarios (username, senha_hash, perfil)
VALUES
    ('comprador', '$2a$10$a62qIgqp0ZKn/sZIUnJL9OeCmZAD.enKwQLgTsytsQhXOLdFxbnJC', 'COMPRADOR'),
    ('aprovador', '$2a$10$O23icpXBc5keNXa9IlfkbuHCghqgA31//DtjlbTdbGcVW7vaUxE/i', 'APROVADOR');
