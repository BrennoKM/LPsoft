package io.lpsoft.core.evento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CriarEventoRequest(
        @NotBlank @Size(max = 200) String titulo,
        @Size(max = 2000) String descricao,
        @NotNull Instant inicio,
        @NotNull Instant fim
) {}
