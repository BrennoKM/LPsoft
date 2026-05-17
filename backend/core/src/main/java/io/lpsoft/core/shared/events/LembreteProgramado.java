package io.lpsoft.core.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado pela feature 'lembretes' quando, ao reagir a um {@link EventoCriado},
 * a política de antecedência define QUANDO o titular deve ser avisado.
 *
 * É um contrato do core (não da feature 'lembretes'): qualquer canal de aviso
 * — toast/in-app ('notificacao'), e-mail, etc. — reage a este fato sem importar
 * tipos da feature que o originou nem depender estritamente dela. A relação é
 * emergente (via contrato do core), como em {@link EventoCriado}: 'lembretes'
 * decide a política; os canais apenas renderizam o que foi programado.
 *
 * {@code quando}: instante em que o aviso deve ocorrer (início do evento menos
 * a antecedência configurada).
 */
public record LembreteProgramado(
        UUID eventoId,
        String titulo,
        Instant quando
) {}
