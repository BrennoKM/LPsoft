package io.lpsoft.features.categorias;

import io.lpsoft.core.shared.events.EventoCriado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Prova o invariante EMERGENTE: 'categorias' reage só ao contrato do core
 * ({@link EventoCriado}, campo origemId) para herdar categorias do modelo —
 * sem conhecer a feature de recorrência.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaHerancaListenerTest {

    @Mock EventoCategoriaRepository vinculos;
    @InjectMocks CategoriaHerancaListener listener;

    private EventoCriado evento(UUID eventoId, UUID origemId) {
        Instant ini = Instant.now();
        return new EventoCriado(eventoId, UUID.randomUUID(), "Ev", ini, ini.plusSeconds(3600), origemId);
    }

    private EventoCategoria vinculo(UUID eventoId, UUID categoriaId) {
        return new EventoCategoria(new EventoCategoria.Id(eventoId, categoriaId));
    }

    @Test
    void sem_origemId_nao_consulta_nem_salva() {
        listener.on(evento(UUID.randomUUID(), null));

        verifyNoInteractions(vinculos);
    }

    @Test
    void com_origemId_copia_todos_os_vinculos_do_modelo() {
        UUID origem = UUID.randomUUID();
        UUID novo = UUID.randomUUID();
        when(vinculos.findByIdEventoId(origem)).thenReturn(List.of(
                vinculo(origem, UUID.randomUUID()),
                vinculo(origem, UUID.randomUUID())));

        listener.on(evento(novo, origem));

        verify(vinculos, org.mockito.Mockito.times(2)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void com_origemId_mas_modelo_sem_categorias_nao_salva() {
        UUID origem = UUID.randomUUID();
        when(vinculos.findByIdEventoId(origem)).thenReturn(List.of());

        listener.on(evento(UUID.randomUUID(), origem));

        verify(vinculos, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void preserva_categoriaId_e_troca_apenas_eventoId() {
        UUID origem = UUID.randomUUID();
        UUID novo = UUID.randomUUID();
        UUID categoria = UUID.randomUUID();
        when(vinculos.findByIdEventoId(origem)).thenReturn(List.of(vinculo(origem, categoria)));

        listener.on(evento(novo, origem));

        ArgumentCaptor<EventoCategoria> captor = ArgumentCaptor.forClass(EventoCategoria.class);
        verify(vinculos).save(captor.capture());
        assertThat(captor.getValue().getId().getEventoId()).isEqualTo(novo);
        assertThat(captor.getValue().getId().getCategoriaId()).isEqualTo(categoria);
    }
}
