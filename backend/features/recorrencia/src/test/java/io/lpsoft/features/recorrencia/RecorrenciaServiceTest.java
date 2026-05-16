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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecorrenciaServiceTest {

    @Mock EventoRecorrenciaRepository repo;
    @Mock EventoRepository eventos;
    @Mock EventoService eventoService;

    RecorrenciaService service(int janela) {
        return new RecorrenciaService(repo, eventos, eventoService, janela);
    }

    RecorrenciaService service() {
        return service(5);
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

        // janela 0: não materializa nada — isola o cálculo do primeiro disparo
        var r = service(0).registrar(id, new CriarRecorrencia(Frequencia.SEMANAL, 2, null));

        assertThat(r.getProximoDisparo()).isEqualTo(Instant.parse("2026-07-15T09:00:00Z"));
        assertThat(r.isAtivo()).isTrue();
        verify(eventoService, never()).criar(any(), any(), any());
    }

    @Test
    void deve_materializar_a_janela_ao_registrar() {
        UUID id = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        when(eventos.findById(id)).thenReturn(Optional.of(evento(id, inicio, inicio.plusSeconds(1800))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var r = service(3).registrar(id, new CriarRecorrencia(Frequencia.DIARIA, 1, null));

        // primeiro disparo = 07-02; materializa 3 (07-02, 07-03, 07-04)
        verify(eventoService, times(3)).criar(any(), any(), any());
        assertThat(r.getProximoDisparo()).isEqualTo(Instant.parse("2026-07-05T09:00:00Z"));
        assertThat(r.isAtivo()).isTrue();
    }

    @Test
    void nao_deve_criar_ocorrencias_no_passado_para_modelo_antigo() {
        UUID id = UUID.randomUUID();
        Instant inicioAntigo = Instant.now().minus(60, ChronoUnit.DAYS);
        when(eventos.findById(id))
                .thenReturn(Optional.of(evento(id, inicioAntigo, inicioAntigo.plusSeconds(1800))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Instant limite = Instant.now().minus(2, ChronoUnit.DAYS);
        var r = service(3).registrar(id, new CriarRecorrencia(Frequencia.DIARIA, 1, null));

        ArgumentCaptor<CriarEventoRequest> cap = ArgumentCaptor.forClass(CriarEventoRequest.class);
        verify(eventoService, times(3)).criar(cap.capture(), any(), any());
        // nenhuma ocorrência no passado distante — todas após "agora"
        assertThat(cap.getAllValues()).allSatisfy(req ->
                assertThat(req.inicio()).isAfter(limite));
        assertThat(r.getProximoDisparo()).isAfter(limite);
        assertThat(r.isAtivo()).isTrue();
    }

    @Test
    void com_ate_materializa_todas_as_ocorrencias_alem_da_janela() {
        UUID id = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        Instant ate = Instant.parse("2026-07-10T12:00:00Z");
        when(eventos.findById(id)).thenReturn(Optional.of(evento(id, inicio, inicio.plusSeconds(1800))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        // janela=2, mas "até" permite ~9 (07-02..07-10): NÃO cabe na janela
        var r = service(2).registrar(id, new CriarRecorrencia(Frequencia.DIARIA, 1, ate));

        verify(eventoService, atLeast(5)).criar(any(), any(), any());
        assertThat(r.isAtivo()).isFalse(); // ultrapassou 'até'
    }

    @Test
    void deve_parar_de_materializar_ao_passar_de_ate() {
        UUID id = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-07-01T09:00:00Z");
        Instant ate = Instant.parse("2026-07-03T23:59:00Z");
        when(eventos.findById(id)).thenReturn(Optional.of(evento(id, inicio, inicio.plusSeconds(1800))));
        when(repo.existsByEventoModeloIdAndAtivoTrue(id)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var r = service(5).registrar(id, new CriarRecorrencia(Frequencia.DIARIA, 1, ate));

        // 07-02 e 07-03 cabem; 07-04 passa de 'ate' → para e desativa
        verify(eventoService, times(2)).criar(any(), any(), any());
        assertThat(r.isAtivo()).isFalse();
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
        verify(eventoService).criar(cap.capture(), any(), any());
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
        verify(eventoService, never()).criar(any(), any(), any());
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
        verify(eventoService).criar(any(), any(), any());
    }
}
