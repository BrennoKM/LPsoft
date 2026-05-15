package io.lpsoft.core.evento.dto;

import io.lpsoft.core.evento.Evento;
import io.lpsoft.core.evento.EventoStatus;

import java.time.Instant;
import java.util.UUID;

public record EventoResponse(
        UUID id,
        String titulo,
        String descricao,
        Instant inicio,
        Instant fim,
        UUID criadoPor,
        EventoStatus status,
        Instant criadoEm
) {
    public static EventoResponse de(Evento e) {
        return new EventoResponse(
                e.getId(),
                e.getTitulo(),
                e.getDescricao(),
                e.getInicio(),
                e.getFim(),
                e.getCriadoPor(),
                e.getStatus(),
                e.getCriadoEm()
        );
    }
}
