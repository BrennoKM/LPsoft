-- Cadastro de usuário.
CREATE TABLE usuario (
    id          UUID         PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    senha_hash  VARCHAR(120) NOT NULL,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_usuario_email ON usuario (LOWER(email));

-- Evento de agenda. criado_por sem FK por design (fronteira de feature),
-- integridade validada na aplicação.
CREATE TABLE evento (
    id            UUID         PRIMARY KEY,
    titulo        VARCHAR(200) NOT NULL,
    descricao     TEXT         NULL,
    inicio        TIMESTAMPTZ  NOT NULL,
    fim           TIMESTAMPTZ  NOT NULL,
    criado_por    UUID         NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_evento_status CHECK (status IN ('RASCUNHO', 'PUBLICADO', 'CANCELADO')),
    CONSTRAINT ck_evento_fim_posterior_inicio CHECK (fim > inicio)
);

CREATE INDEX ix_evento_criado_por ON evento (criado_por);
CREATE INDEX ix_evento_inicio ON evento (inicio);
