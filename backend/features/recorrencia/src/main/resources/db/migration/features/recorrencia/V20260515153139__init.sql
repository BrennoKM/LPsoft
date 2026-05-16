-- Schema da feature 'recorrencia'. Roda apenas quando a feature está no build.
-- evento_modelo_id é FK INFORMAL para evento(id): sem REFERENCES, integridade
-- validada na aplicação. Mantém a feature desacoplada do schema do core
-- (mesma decisão de evento_categoria; é a fronteira que microserviço exigiria).

CREATE TABLE evento_recorrencia (
    id                UUID         PRIMARY KEY,
    evento_modelo_id  UUID         NOT NULL,
    freq              VARCHAR(10)  NOT NULL,
    intervalo         INT          NOT NULL DEFAULT 1,
    proximo_disparo   TIMESTAMPTZ  NOT NULL,
    ate               TIMESTAMPTZ,
    ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_recorrencia_freq CHECK (freq IN ('DIARIA', 'SEMANAL', 'MENSAL')),
    CONSTRAINT ck_recorrencia_intervalo CHECK (intervalo >= 1)
);

CREATE INDEX ix_recorrencia_pendente ON evento_recorrencia (proximo_disparo) WHERE ativo;
CREATE INDEX ix_recorrencia_evento ON evento_recorrencia (evento_modelo_id);
