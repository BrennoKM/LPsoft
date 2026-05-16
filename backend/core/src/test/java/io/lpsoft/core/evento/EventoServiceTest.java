package io.lpsoft.core.evento;

import io.lpsoft.core.evento.dto.AtualizarEventoRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock EventoRepository repo;
    @Mock ApplicationEventPublisher publisher;

    EventoService service() {
        return new EventoService(repo, publisher);
    }

    private Evento evento(UUID id, UUID dono) {
        Instant agora = Instant.now();
        return Evento.builder()
                .id(id).titulo("Antigo").descricao("d")
                .inicio(agora).fim(agora.plusSeconds(3600)).criadoPor(dono)
                .status(EventoStatus.RASCUNHO).criadoEm(agora).atualizadoEm(agora)
                .build();
    }

    @Test
    void deve_atualizar_evento_do_dono() {
        UUID id = UUID.randomUUID();
        UUID dono = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(evento(id, dono)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Instant ini = Instant.parse("2026-09-01T09:00:00Z");
        var resp = service().atualizar(
                id, new AtualizarEventoRequest("Novo", "nd", ini, ini.plusSeconds(1800)), dono);

        assertThat(resp.titulo()).isEqualTo("Novo");
        assertThat(resp.inicio()).isEqualTo(ini);
        verify(repo).save(any());
    }

    @Test
    void nao_deve_atualizar_evento_de_outro_dono() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(evento(id, UUID.randomUUID())));

        Instant ini = Instant.now();
        assertThatThrownBy(() -> service().atualizar(
                id, new AtualizarEventoRequest("X", null, ini, ini.plusSeconds(60)), UUID.randomUUID()))
                .isInstanceOf(EventoNaoEncontradoException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void deve_rejeitar_atualizacao_com_fim_antes_do_inicio() {
        Instant ini = Instant.now();
        assertThatThrownBy(() -> service().atualizar(
                UUID.randomUUID(),
                new AtualizarEventoRequest("X", null, ini, ini.minusSeconds(60)),
                UUID.randomUUID()))
                .isInstanceOf(EventoInvalidoException.class);
    }

    @Test
    void deve_excluir_evento_do_dono() {
        UUID id = UUID.randomUUID();
        UUID dono = UUID.randomUUID();
        Evento e = evento(id, dono);
        when(repo.findById(id)).thenReturn(Optional.of(e));

        service().excluir(id, dono);

        verify(repo).delete(e);
    }

    @Test
    void nao_deve_excluir_evento_de_outro_dono() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.of(evento(id, UUID.randomUUID())));

        assertThatThrownBy(() -> service().excluir(id, UUID.randomUUID()))
                .isInstanceOf(EventoNaoEncontradoException.class);
        verify(repo, never()).delete(any());
    }
}
