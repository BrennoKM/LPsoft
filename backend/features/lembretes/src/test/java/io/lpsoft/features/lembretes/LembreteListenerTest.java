package io.lpsoft.features.lembretes;

import io.lpsoft.core.shared.events.EventoCriado;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LembreteListenerTest {

    private final AgendadorLembrete agendador = new AgendadorLembrete();
    private final LembreteListener listener = new LembreteListener(agendador);

    @Test
    void deve_agendar_lembrete_24h_antes_do_inicio() {
        UUID eventoId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        listener.on(new EventoCriado(eventoId, UUID.randomUUID(), "Reunião", inicio, inicio.plus(1, ChronoUnit.HOURS)));

        assertThat(agendador.lembretesAgendados())
                .singleElement()
                .satisfies(l -> {
                    assertThat(l.eventoId()).isEqualTo(eventoId);
                    assertThat(l.titulo()).isEqualTo("Reunião");
                    assertThat(l.quando()).isEqualTo(inicio.minus(Duration.ofHours(24)));
                });
    }

    @Test
    void deve_agendar_um_lembrete_por_evento() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.DAYS);
        listener.on(new EventoCriado(UUID.randomUUID(), UUID.randomUUID(), "A", inicio, inicio.plusSeconds(3600)));
        listener.on(new EventoCriado(UUID.randomUUID(), UUID.randomUUID(), "B", inicio, inicio.plusSeconds(3600)));

        assertThat(agendador.lembretesAgendados()).hasSize(2);
    }
}
