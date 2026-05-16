package io.lpsoft.core.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado por {@code EventoService.criar} após persistir um novo Evento.
 * Contrato consumido por listeners em features opcionais.
 *
 * {@code origemId}: quando o evento deriva de outro (ex.: ocorrência gerada
 * por uma recorrência) aponta para a RAIZ original (o modelo) — é sempre o
 * evento-pai, nunca encadeia ocorrência→ocorrência (estrela, profundidade 1).
 * {@code null} para eventos criados diretamente. Features podem reagir
 * (ex.: categorias herdam as do modelo) sem que core ou a recorrência
 * conheçam a feature.
 */
public record EventoCriado(
        UUID eventoId,
        UUID criadoPor,
        String titulo,
        Instant inicio,
        Instant fim,
        UUID origemId
) {
    /** Evento sem origem (criado diretamente) — mantém chamadas antigas válidas. */
    public EventoCriado(UUID eventoId, UUID criadoPor, String titulo, Instant inicio, Instant fim) {
        this(eventoId, criadoPor, titulo, inicio, fim, null);
    }
}
