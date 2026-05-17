package io.lpsoft.features.categorias;

import io.lpsoft.core.shared.events.EventoCriado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quando um evento nasce derivado de outro ({@code origemId} != null — ex.:
 * ocorrência de recorrência), herda as categorias do modelo. Reage apenas ao
 * contrato do core; não conhece a feature de recorrência. Só existe quando o
 * módulo 'categorias' está no build.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CategoriaHerancaListener {

    private final EventoCategoriaRepository vinculos;

    @EventListener
    @Transactional
    public void on(EventoCriado evento) {
        if (evento.origemId() == null) {
            return;
        }
        var doModelo = vinculos.findByIdEventoId(evento.origemId());
        for (EventoCategoria v : doModelo) {
            vinculos.save(new EventoCategoria(
                    new EventoCategoria.Id(evento.eventoId(), v.getId().getCategoriaId())));
        }
        if (!doModelo.isEmpty()) {
            log.debug("Evento {} herdou {} categoria(s) de {}",
                    evento.eventoId(), doModelo.size(), evento.origemId());
        }
    }
}
