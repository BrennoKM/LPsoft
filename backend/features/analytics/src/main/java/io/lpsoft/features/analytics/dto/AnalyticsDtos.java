package io.lpsoft.features.analytics.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    public record PorUsuario(UUID criadoPor, long total) {}

    public record PorDia(LocalDate dia, long total) {}

    public record ResumoResponse(
            long total,
            List<PorUsuario> porUsuario,
            List<PorDia> porDia
    ) {}
}
