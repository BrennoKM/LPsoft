package io.lpsoft.features.recorrencia;

import java.time.Instant;
import java.time.ZoneOffset;

public enum Frequencia {
    DIARIA,
    SEMANAL,
    MENSAL;

    /** Avança um instante por {@code intervalo} unidades desta frequência (UTC). */
    public Instant avancar(Instant base, int intervalo) {
        return switch (this) {
            case DIARIA -> base.atZone(ZoneOffset.UTC).plusDays(intervalo).toInstant();
            case SEMANAL -> base.atZone(ZoneOffset.UTC).plusWeeks(intervalo).toInstant();
            case MENSAL -> base.atZone(ZoneOffset.UTC).plusMonths(intervalo).toInstant();
        };
    }
}
