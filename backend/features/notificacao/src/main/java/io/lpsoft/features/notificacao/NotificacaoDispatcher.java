package io.lpsoft.features.notificacao;

import io.lpsoft.features.notificacao.NotificacaoRegistry.NotificacaoProgramada;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Fecha o ciclo do canal: periodicamente despacha as notificações cuja hora
 * programada já chegou, marcando-as como enviadas (envio simulado — log).
 * Numa implementação real seria o disparo do toast/in-app de fato.
 *
 * Só existe quando a feature está no build (NotificacaoSchedulingConfig
 * habilita o scheduler). Intervalo configurável via
 * features.notificacao.intervalo-ms (default 30s).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoDispatcher {

    private final NotificacaoRegistry registry;

    @Scheduled(fixedDelayString = "${features.notificacao.intervalo-ms:30000}")
    public void despachar() {
        List<NotificacaoProgramada> enviadas = registry.despacharAte(Instant.now());
        for (NotificacaoProgramada n : enviadas) {
            log.info("Notificação enviada (simulado) para evento '{}' ({})",
                    n.titulo(), n.eventoId());
        }
    }
}
