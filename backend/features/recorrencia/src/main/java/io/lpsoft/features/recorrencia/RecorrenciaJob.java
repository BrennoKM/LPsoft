package io.lpsoft.features.recorrencia;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara o processamento periódico das regras. Só existe quando a feature
 * está no build (e RecorrenciaSchedulingConfig habilita o scheduler).
 * Intervalo configurável via features.recorrencia.intervalo-ms (default 60s).
 */
@Component
@RequiredArgsConstructor
public class RecorrenciaJob {

    private final RecorrenciaService service;

    @Scheduled(fixedDelayString = "${features.recorrencia.intervalo-ms:60000}")
    public void processar() {
        service.processarPendentes();
    }
}
