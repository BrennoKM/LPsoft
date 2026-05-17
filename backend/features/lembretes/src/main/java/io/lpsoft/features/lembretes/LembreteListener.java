package io.lpsoft.features.lembretes;

import io.lpsoft.core.shared.events.EventoCriado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Escuta {@link EventoCriado} (contrato do core) e programa um lembrete
 * conforme a {@link PoliticaLembrete} (antecedência configurável). Não conhece
 * o EventoService nem o banco do core — apenas o contrato publicado.
 *
 * Carregado por classpath scanning quando o módulo está no build.
 * {@code matchIfMissing = true}: ativo por padrão; desligável por config sem rebuild.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "features.lembretes", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LembreteListener {

    private final PoliticaLembrete politica;
    private final AgendadorLembrete agendador;

    @EventListener
    public void on(EventoCriado evento) {
        Instant quando = evento.inicio().minus(politica.antecedencia());
        log.debug("EventoCriado recebido: {} — programando lembrete", evento.eventoId());
        agendador.agendar(evento.eventoId(), evento.titulo(), quando);
    }
}
