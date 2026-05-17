package io.lpsoft.features.lembretes.dto;

import io.lpsoft.features.lembretes.AgendadorLembrete.LembreteAgendado;

import java.time.Instant;
import java.util.UUID;

public final class LembreteDtos {

    private LembreteDtos() {}

    /** Política de antecedência (em horas). */
    public record PoliticaResponse(long antecedenciaHoras) {}

    public record AtualizarPoliticaRequest(Long antecedenciaHoras) {}

    public record LembreteAgendadoResponse(UUID eventoId, String titulo, Instant quando) {
        public static LembreteAgendadoResponse de(LembreteAgendado l) {
            return new LembreteAgendadoResponse(l.eventoId(), l.titulo(), l.quando());
        }
    }
}
