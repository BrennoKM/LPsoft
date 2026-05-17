package io.lpsoft.features.notificacao;

import io.lpsoft.core.shared.events.LembreteProgramado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Canal de aviso toast/in-app. Reage a {@link LembreteProgramado} — contrato
 * do CORE — registrando uma notificação programada. Importa apenas tipos do
 * core: não conhece nem depende estritamente da feature 'lembretes' (relação
 * EMERGENTE via contrato do core). 'lembretes' decide a política; este é só um
 * canal que renderiza o que foi programado — outro canal (e-mail) reagiria ao
 * mesmo contrato sem nenhuma alteração aqui.
 *
 * Carregado por classpath scanning quando o módulo está no build.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "features.notificacao", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NotificacaoListener {

    private final NotificacaoRegistry registry;

    @EventListener
    public void on(LembreteProgramado evento) {
        registry.registrar(evento.eventoId(), evento.titulo(), evento.quando());
        log.info("Notificação programada para evento '{}' ({}) em {}",
                evento.titulo(), evento.eventoId(), evento.quando());
    }
}
