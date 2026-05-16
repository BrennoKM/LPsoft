package io.lpsoft.features.lembretes;

import java.time.Instant;
import java.util.UUID;

/**
 * Contrato publicado por {@link AgendadorLembrete} quando um lembrete é
 * agendado. Pertence à feature 'lembretes'. Features que reagem a isto
 * (ex.: push-notif) dependem estritamente do módulo 'lembretes'.
 */
public record LembreteAgendadoEvent(
        UUID eventoId,
        String titulo,
        Instant quando
) {}
