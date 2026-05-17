package io.lpsoft.features.notificacao.dto;

import io.lpsoft.features.notificacao.NotificacaoRegistry.NotificacaoProgramada;

import java.time.Instant;
import java.util.UUID;

public final class NotificacaoDtos {

    private NotificacaoDtos() {}

    public record NotificacaoResponse(
            UUID eventoId,
            String titulo,
            Instant programadaPara,
            Instant enviadaEm) {
        public static NotificacaoResponse de(NotificacaoProgramada n) {
            return new NotificacaoResponse(
                    n.eventoId(), n.titulo(), n.programadaPara(), n.enviadaEm());
        }
    }
}
