package io.lpsoft.features.recorrencia.dto;

import io.lpsoft.features.recorrencia.EventoRecorrencia;
import io.lpsoft.features.recorrencia.Frequencia;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public final class RecorrenciaDtos {

    private RecorrenciaDtos() {}

    public record CriarRecorrencia(
            @NotNull Frequencia freq,
            @Min(1) int intervalo,
            Instant ate
    ) {}

    public record RecorrenciaResponse(
            UUID id,
            UUID eventoModeloId,
            Frequencia freq,
            int intervalo,
            Instant proximoDisparo,
            Instant ate,
            boolean ativo
    ) {
        public static RecorrenciaResponse de(EventoRecorrencia r) {
            return new RecorrenciaResponse(
                    r.getId(), r.getEventoModeloId(), r.getFreq(), r.getIntervalo(),
                    r.getProximoDisparo(), r.getAte(), r.isAtivo()
            );
        }
    }

    public record ProcessamentoResponse(int ocorrenciasCriadas) {}
}
