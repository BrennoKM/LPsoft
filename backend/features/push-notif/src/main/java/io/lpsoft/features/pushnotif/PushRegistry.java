package io.lpsoft.features.pushnotif;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Guarda em memória os pushes "enviados" (simulado). Numa implementação real
 * aqui entraria FCM/APNs/WebPush.
 */
@Component
public class PushRegistry {

    public record PushEnviado(UUID eventoId, String titulo, Instant agendadoPara, Instant enviadoEm) {}

    private final List<PushEnviado> enviados = new CopyOnWriteArrayList<>();

    public void registrar(PushEnviado push) {
        enviados.add(push);
    }

    public List<PushEnviado> enviados() {
        return List.copyOf(enviados);
    }
}
