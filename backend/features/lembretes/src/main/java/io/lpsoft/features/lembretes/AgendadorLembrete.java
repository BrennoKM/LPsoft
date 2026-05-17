package io.lpsoft.features.lembretes;

import io.lpsoft.core.shared.events.LembreteProgramado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registra os lembretes programados em memória (a lista exibida na tela) e
 * publica {@link LembreteProgramado} — contrato do core — para que canais de
 * aviso (toast/in-app, e-mail, …) reajam sem conhecer esta feature.
 * Numa implementação real, aqui entraria fila/scheduler.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AgendadorLembrete {

    public record LembreteAgendado(UUID eventoId, String titulo, Instant quando) {}

    private final ApplicationEventPublisher publisher;
    private final List<LembreteAgendado> agendados = new CopyOnWriteArrayList<>();

    public void agendar(UUID eventoId, String titulo, Instant quando) {
        LembreteAgendado lembrete = new LembreteAgendado(eventoId, titulo, quando);
        agendados.add(lembrete);
        log.info("Lembrete programado para evento '{}' ({}) em {}", titulo, eventoId, quando);
        publisher.publishEvent(new LembreteProgramado(eventoId, titulo, quando));
    }

    public List<LembreteAgendado> lembretesAgendados() {
        return List.copyOf(agendados);
    }
}
