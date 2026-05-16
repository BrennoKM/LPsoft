-- Schema próprio da feature 'analytics'. Roda APENAS quando a feature está
-- no build (location adicionada por AnalyticsFlywayConfig).

-- criado_por sem FK para usuario(id) por design: a feature não acopla seu
-- schema ao do core. É um agregado derivado do contrato de eventos.
CREATE TABLE analytics_evento_diario (
    criado_por  UUID    NOT NULL,
    dia         DATE    NOT NULL,
    total       INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (criado_por, dia)
);

CREATE INDEX ix_analytics_dia ON analytics_evento_diario (dia);
