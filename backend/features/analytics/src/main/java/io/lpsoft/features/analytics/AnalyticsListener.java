package io.lpsoft.features.analytics;

import io.lpsoft.core.shared.events.EventoCriado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Onividente: escuta {@link EventoCriado} (contrato do core) e incrementa o
 * agregado (usuário, dia). Não conhece nenhuma outra feature nem o banco do
 * core — só o contrato publicado.
 *
 * Carregado por classpath scanning quando o módulo está no build.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "features.analytics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsListener {

    private final EventoDiarioRepository repo;

    @EventListener
    @Transactional
    public void on(EventoCriado evento) {
        LocalDate dia = evento.inicio().atZone(ZoneOffset.UTC).toLocalDate();
        EventoDiario.Id chave = new EventoDiario.Id(evento.criadoPor(), dia);
        EventoDiario agregado = repo.findById(chave)
                .orElseGet(() -> new EventoDiario(chave, 0));
        agregado.setTotal(agregado.getTotal() + 1);
        repo.save(agregado);
        log.debug("EventoCriado contabilizado: usuário {} dia {} → total {}",
                evento.criadoPor(), dia, agregado.getTotal());
    }
}
