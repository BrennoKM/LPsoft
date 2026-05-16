-- origem_id: raiz da qual o evento deriva (ex.: ocorrência de recorrência
-- aponta para o evento modelo). Soft-reference informal — sem FK física,
-- pode pendurar se o modelo for apagado (coerente com o resto do domínio).
-- NULL para eventos criados diretamente.
ALTER TABLE evento ADD COLUMN origem_id UUID;

CREATE INDEX ix_evento_origem ON evento (origem_id);
