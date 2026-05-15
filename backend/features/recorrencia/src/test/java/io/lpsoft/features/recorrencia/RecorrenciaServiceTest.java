package io.lpsoft.features.recorrencia;

import io.lpsoft.core.evento.Evento;
import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.core.evento.EventoService;
import io.lpsoft.core.evento.EventoStatus;
import io.lpsoft.core.evento.dto.CriarEventoRequest;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaJaExiste;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.CriarRecorrencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecorrenciaServiceTest {

    @Mock EventoRecorrenciaRepository repo;
    @Mock EventoRepository eventos;
    @Mock EventoService eventoService;

    RecorrenciaService service() {
        return new RecorrenciaService(repo, eventos, eventoService);
    }

    private Evento evento(UUID id, Instant inicio, Instant fim) {
        return Evento.builder()
                .id(id).titulo("Daily").descricao("standup")
                .inicio(inicio).fim(fim).criadoPor(UUID.randomUUID())
                .status(EventoStatus.RASCUNHO).criadoEm(Instant.now()).atualizadoEm(Instant.now())
                .build();
    }

    @Test
    void deve_rejeitar_registro_quando_evento_nao_existe() {
        UUID id = UUID.randomUUID();
        when(eventos.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().registrar(id, new CriarRecorrencia(Frequencia.DIARIA, 1, null)))
                .isInstanceOf(EventoNaoEncontrado.class);
    }

    @Test
    void deve_rejeitar_registro_duplicado() {
        UUID id = UUID.randomUUID();
        Instant inicio = Instant.now().plus(1, ChronoUnit.DAYS);
        when(eventos.findById(id)).thenReturn(Optional.of(evento(id, inicio, inicio.plusSeconds(3600))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(true);

        assertThatThrownBy(() -> service().registrar(id, new CriarRecorrencia(Frequencia.SEMANAL, 1, null)))
                .isInstanceOf(RecorrenciaJaExiste.class);
    }

    @Test
    void deve_calcular_primeiro_disparo_a_partir_do_inicio_do_modelo() {
        UUID id = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        when(eventos.findById(id)).thenReturn(Optional.of(evento(id, inicio, inicio.plusSeconds(1800))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var r = service().registrar(id, new CriarRecorrencia(Frequencia.SEMANAL, 2, null));

        assertThat(r.getProximoDisparo()).isEqualTo(Instant.parse("2026-07-15T09:00:00Z"));
        assertThat(r.isAtivo()).isTrue();
    }

    @Test
    void deve_criar_ocorrencia_no_core_e_avancar_disparo() {
        UUID eventoId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        Instant fim = inicio.plus(Duration.ofMinutes(30));
        Instant disparo = Instant.parse("2026-07-02T09:00:00Z");

        var regra = EventoRecorrencia.builder()
                .id(UUID.randomUUID()).eventoModeloId(eventoId).freq(Frequencia.DIARIA)
                .intervalo(1).proximoDisparo(disparo).ativo(true).criadoEm(Instant.now()).build();

        when(repo.findByAtivoTrueAndProximoDisparoLessThanEqual(any())).thenReturn(List.of(regra));
        when(eventos.findById(eventoId)).thenReturn(Optional.of(evento(eventoId, inicio, fim)));

        int criadas = service().processarPendentes();

        assertThat(criadas).isEqualTo(1);
        ArgumentCaptor<CriarEventoRequest> cap = ArgumentCaptor.forClass(CriarEventoRequest.class);
        verify(eventoService).criar(cap.capture(), any());
        assertThat(cap.getValue().inicio()).isEqualTo(disparo);
        assertThat(cap.getValue().fim()).isEqualTo(disparo.plus(Duration.ofMinutes(30)));
        assertThat(regra.getProximoDisparo()).isEqualTo(Instant.parse("2026-07-03T09:00:00Z"));
    }

    @Test
    void deve_desativar_regra_orfa_quando_evento_modelo_sumiu() {
        UUID eventoId = UUID.randomUUID();
        var regra = EventoRecorrencia.builder()
                .id(UUID.randomUUID()).eventoModeloId(eventoId).freq(Frequencia.DIARIA)
                .intervalo(1).proximoDisparo(Instant.now().minusSeconds(60)).ativo(true)
                .criadoEm(Instant.now()).build();

        when(repo.findByAtivoTrueAndProximoDisparoLessThanEqual(any())).thenReturn(List.of(regra));
        when(eventos.findById(eventoId)).thenReturn(Optional.empty());

        int criadas = service().processarPendentes();

        assertThat(criadas).isZero();
        assertThat(regra.isAtivo()).isFalse();
        verify(eventoService, never()).criar(any(), any());
    }

    @Test
    void deve_desativar_regra_quando_proximo_passa_de_ate() {
        UUID eventoId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        Instant disparo = Instant.parse("2026-07-02T09:00:00Z");
        var regra = EventoRecorrencia.builder()
                .id(UUID.randomUUID()).eventoModeloId(eventoId).freq(Frequencia.DIARIA)
                .intervalo(1).proximoDisparo(disparo).ate(Instant.parse("2026-07-02T12:00:00Z"))
                .ativo(true).criadoEm(Instant.now()).build();

        when(repo.findByAtivoTrueAndProximoDisparoLessThanEqual(any())).thenReturn(List.of(regra));
        when(eventos.findById(eventoId)).thenReturn(Optional.of(evento(eventoId, inicio, inicio.plusSeconds(1800))));

        service().processarPendentes();

        assertThat(regra.isAtivo()).isFalse();
        verify(eventoService).criar(any(), any());
    }
}
