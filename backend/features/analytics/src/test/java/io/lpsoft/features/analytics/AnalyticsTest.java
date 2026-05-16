package io.lpsoft.features.analytics;

import io.lpsoft.core.shared.events.EventoCriado;
import io.lpsoft.features.analytics.dto.AnalyticsDtos.ResumoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsTest {

    @Mock EventoDiarioRepository repo;

    private EventoCriado evento(UUID criadoPor, Instant inicio) {
        return new EventoCriado(UUID.randomUUID(), criadoPor, "Reunião", inicio, inicio.plusSeconds(3600));
    }

    @Test
    void primeiro_evento_do_dia_cria_agregado_com_total_1() {
        UUID usuario = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        when(repo.findById(any())).thenReturn(Optional.empty());

        new AnalyticsListener(repo).on(evento(usuario, inicio));

        ArgumentCaptor<EventoDiario> cap = ArgumentCaptor.forClass(EventoDiario.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getId().getCriadoPor()).isEqualTo(usuario);
        assertThat(cap.getValue().getId().getDia()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(cap.getValue().getTotal()).isEqualTo(1);
    }

    @Test
    void evento_no_mesmo_dia_incrementa_agregado_existente() {
        UUID usuario = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T23:30:00Z");
        var existente = new EventoDiario(new EventoDiario.Id(usuario, LocalDate.of(2026, 7, 1)), 4);
        when(repo.findById(any())).thenReturn(Optional.of(existente));

        new AnalyticsListener(repo).on(evento(usuario, inicio));

        verify(repo).save(existente);
        assertThat(existente.getTotal()).isEqualTo(5);
    }

    @Test
    void resumo_soma_total_e_agrupa_por_usuario_e_dia() {
        UUID ana = UUID.randomUUID();
        UUID bia = UUID.randomUUID();
        LocalDate d1 = LocalDate.of(2026, 7, 1);
        LocalDate d2 = LocalDate.of(2026, 7, 2);
        when(repo.findAll()).thenReturn(List.of(
                new EventoDiario(new EventoDiario.Id(ana, d1), 3),
                new EventoDiario(new EventoDiario.Id(ana, d2), 2),
                new EventoDiario(new EventoDiario.Id(bia, d1), 6)
        ));

        ResumoResponse r = new AnalyticsService(repo).resumo();

        assertThat(r.total()).isEqualTo(11);
        assertThat(r.porUsuario()).first()
                .satisfies(u -> {
                    assertThat(u.criadoPor()).isEqualTo(bia);
                    assertThat(u.total()).isEqualTo(6);
                });
        assertThat(r.porDia()).extracting("dia").containsExactly(d1, d2);
        assertThat(r.porDia()).extracting("total").containsExactly(9L, 2L);
    }
}
