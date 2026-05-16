package io.lpsoft.core.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por {@code EventoService.criar} após persistir um novo Evento em
 * status RASCUNHO. Contrato consumido por listeners em features opcionais.
 */
public record EventoCriado(
        UUID eventoId,
        UUID criadoPor,
        String titulo,
        Instant inicio,
        Instant fim
) {}
