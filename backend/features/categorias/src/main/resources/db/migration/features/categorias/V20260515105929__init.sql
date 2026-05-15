-- Schema próprio da feature 'categorias'. Roda APENAS quando a feature
-- está no build (location adicionada por CategoriasFlywayConfig).

CREATE TABLE categoria (
    id         UUID         PRIMARY KEY,
    nome       VARCHAR(80)  NOT NULL,
    cor        VARCHAR(7),
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_categoria_nome ON categoria (LOWER(nome));

-- evento_id sem FK para evento(id) por design: a feature não acopla seu
-- schema ao do core. Integridade validada na aplicação.
CREATE TABLE evento_categoria (
    evento_id     UUID NOT NULL,
    categoria_id  UUID NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
    PRIMARY KEY (evento_id, categoria_id)
);

CREATE INDEX ix_evento_categoria_evento ON evento_categoria (evento_id);
