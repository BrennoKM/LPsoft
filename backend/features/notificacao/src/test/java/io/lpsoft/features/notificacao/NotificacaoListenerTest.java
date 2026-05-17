package io.lpsoft.features.notificacao;

import io.lpsoft.core.shared.events.LembreteProgramado;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificacaoListenerTest {

    private final NotificacaoRegistry registry = new NotificacaoRegistry();
    private final NotificacaoListener listener = new NotificacaoListener(registry);
    private final NotificacaoDispatcher dispatcher = new NotificacaoDispatcher(registry);

    @Test
    void deve_registrar_notificacao_programada_e_nao_enviada_ao_receber_lembrete() {
        UUID eventoId = UUID.randomUUID();
        Instant quando = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

        listener.on(new LembreteProgramado(eventoId, "Reunião", quando));

        assertThat(registry.todas())
                .singleElement()
                .satisfies(n -> {
                    assertThat(n.eventoId()).isEqualTo(eventoId);
                    assertThat(n.titulo()).isEqualTo("Reunião");
                    assertThat(n.programadaPara()).isEqualTo(quando);
                    assertThat(n.enviada()).isFalse();
                    assertThat(n.enviadaEm()).isNull();
                });
    }

    @Test
    void deve_acumular_uma_notificacao_por_lembrete() {
        Instant quando = Instant.now().plus(2, ChronoUnit.DAYS);
        listener.on(new LembreteProgramado(UUID.randomUUID(), "A", quando));
        listener.on(new LembreteProgramado(UUID.randomUUID(), "B", quando));

        assertThat(registry.todas()).hasSize(2);
    }

    @Test
    void dispatcher_envia_apenas_as_vencidas() {
        listener.on(new LembreteProgramado(UUID.randomUUID(), "Vencida",
                Instant.now().minus(1, ChronoUnit.MINUTES)));
        listener.on(new LembreteProgramado(UUID.randomUUID(), "Futura",
                Instant.now().plus(1, ChronoUnit.DAYS)));

        dispatcher.despachar();

        assertThat(registry.todas())
                .filteredOn(NotificacaoRegistry.NotificacaoProgramada::enviada)
                .singleElement()
                .satisfies(n -> {
                    assertThat(n.titulo()).isEqualTo("Vencida");
                    assertThat(n.enviadaEm()).isNotNull();
                });
    }

    @Test
    void dispatcher_e_idempotente_nao_reenvia() {
        listener.on(new LembreteProgramado(UUID.randomUUID(), "Vencida",
                Instant.now().minus(1, ChronoUnit.MINUTES)));

        dispatcher.despachar();
        Instant primeiraVez = registry.todas().get(0).enviadaEm();
        dispatcher.despachar();

        assertThat(registry.todas().get(0).enviadaEm()).isEqualTo(primeiraVez);
    }
}
