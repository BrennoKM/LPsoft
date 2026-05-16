package io.lpsoft.features.lembretes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registra lembretes em memória e simula o envio (log). Publica
 * {@link LembreteAgendadoEvent} para features que reagem ao agendamento.
 * Numa implementação real, aqui entraria fila/SMTP/push.
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
        log.info("Lembrete agendado para evento '{}' ({}) em {}", titulo, eventoId, quando);
        publisher.publishEvent(new LembreteAgendadoEvent(eventoId, titulo, quando));
    }

    public List<LembreteAgendado> lembretesAgendados() {
        return List.copyOf(agendados);
    }
}
