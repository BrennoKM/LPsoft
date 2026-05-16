package io.lpsoft.features.pushnotif;

import io.lpsoft.features.lembretes.LembreteAgendadoEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PushNotifListenerTest {

    private final PushRegistry registry = new PushRegistry();
    private final PushNotifListener listener = new PushNotifListener(registry);

    @Test
    void deve_registrar_push_ao_receber_lembrete_agendado() {
        UUID eventoId = UUID.randomUUID();
        Instant quando = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        listener.on(new LembreteAgendadoEvent(eventoId, "Reunião", quando));

        assertThat(registry.enviados())
                .singleElement()
                .satisfies(p -> {
                    assertThat(p.eventoId()).isEqualTo(eventoId);
                    assertThat(p.titulo()).isEqualTo("Reunião");
                    assertThat(p.agendadoPara()).isEqualTo(quando);
                    assertThat(p.enviadoEm()).isNotNull();
                });
    }

    @Test
    void deve_acumular_um_push_por_lembrete() {
        Instant quando = Instant.now().plus(2, ChronoUnit.DAYS);
        listener.on(new LembreteAgendadoEvent(UUID.randomUUID(), "A", quando));
        listener.on(new LembreteAgendadoEvent(UUID.randomUUID(), "B", quando));

        assertThat(registry.enviados()).hasSize(2);
    }
}
