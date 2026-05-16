package io.lpsoft.core.evento;

import io.lpsoft.core.evento.dto.CriarEventoRequest;
import io.lpsoft.core.evento.dto.EventoResponse;
import io.lpsoft.core.shared.events.EventoCriado;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository repo;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public EventoResponse criar(CriarEventoRequest req, UUID criadoPor) {
        if (!req.fim().isAfter(req.inicio())) {
            throw new EventoInvalidoException("Fim do evento deve ser posterior ao início");
        }
        Instant agora = Instant.now();
        Evento e = Evento.builder()
                .id(UUID.randomUUID())
                .titulo(req.titulo())
                .descricao(req.descricao())
                .inicio(req.inicio())
                .fim(req.fim())
                .criadoPor(criadoPor)
                .status(EventoStatus.RASCUNHO)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        Evento salvo = repo.save(e);
        publisher.publishEvent(new EventoCriado(
                salvo.getId(), salvo.getCriadoPor(), salvo.getTitulo(),
                salvo.getInicio(), salvo.getFim()
        ));
        return EventoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarDoUsuario(UUID usuarioId) {
        return repo.findByCriadoPorOrderByInicioAsc(usuarioId)
                .stream()
                .map(EventoResponse::de)
                .toList();
    }
}
