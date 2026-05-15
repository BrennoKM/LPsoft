package io.lpsoft.core.auth.dto;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID usuarioId,
        String email
) {}
