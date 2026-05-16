package io.lpsoft.features.pushnotif.dto;

import io.lpsoft.features.pushnotif.PushRegistry.PushEnviado;

import java.time.Instant;
import java.util.UUID;

public final class PushNotifDtos {

    private PushNotifDtos() {}

    public record PushResponse(UUID eventoId, String titulo, Instant agendadoPara, Instant enviadoEm) {
        public static PushResponse de(PushEnviado p) {
            return new PushResponse(p.eventoId(), p.titulo(), p.agendadoPara(), p.enviadoEm());
        }
    }
}
