package io.lpsoft.features.pushnotif;

import io.lpsoft.features.lembretes.LembreteAgendadoEvent;
import io.lpsoft.features.pushnotif.PushRegistry.PushEnviado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Reage ao contrato de 'lembretes' ({@link LembreteAgendadoEvent}) e "envia"
 * um push. O import de um tipo de outra feature é a dependência estrita —
 * sem o módulo 'lembretes' no build, esta classe não compila.
 *
 * Carregado por classpath scanning quando o módulo está no build.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "features.push-notif", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PushNotifListener {

    private final PushRegistry registry;

    @EventListener
    public void on(LembreteAgendadoEvent evento) {
        PushEnviado push = new PushEnviado(
                evento.eventoId(), evento.titulo(), evento.quando(), Instant.now());
        registry.registrar(push);
        log.info("Push enviado para evento '{}' ({})", evento.titulo(), evento.eventoId());
    }
}
