package io.lpsoft.core.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("lpsoft.jwt")
@Validated
public record JwtProperties(
        @NotBlank String secret,
        @Positive int expirationHours,
        @NotBlank String issuer
) {}
