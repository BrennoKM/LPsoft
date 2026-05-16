package io.lpsoft.core.evento;

import io.lpsoft.core.evento.dto.AtualizarEventoRequest;
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

    public EventoResponse criar(CriarEventoRequest req, UUID criadoPor) {
        return criar(req, criadoPor, null);
    }

    /**
     * Cria um evento, opcionalmente derivado de uma raiz ({@code origemId} —
     * ex.: ocorrência de recorrência). O id da origem viaja no contrato
     * {@link EventoCriado} para features reagirem (ex.: herdar categorias)
     * sem que core conheça a feature.
     */
    @Transactional
    public EventoResponse criar(CriarEventoRequest req, UUID criadoPor, UUID origemId) {
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
                .origemId(origemId)
                .status(EventoStatus.RASCUNHO)
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();
        Evento salvo = repo.save(e);
        publisher.publishEvent(new EventoCriado(
                salvo.getId(), salvo.getCriadoPor(), salvo.getTitulo(),
                salvo.getInicio(), salvo.getFim(), salvo.getOrigemId()
        ));
        return EventoResponse.de(salvo);
    }

    @Transactional
    public EventoResponse atualizar(UUID id, AtualizarEventoRequest req, UUID dono) {
        if (!req.fim().isAfter(req.inicio())) {
            throw new EventoInvalidoException("Fim do evento deve ser posterior ao início");
        }
        Evento e = doDono(id, dono);
        e.setTitulo(req.titulo());
        e.setDescricao(req.descricao());
        e.setInicio(req.inicio());
        e.setFim(req.fim());
        e.setAtualizadoEm(Instant.now());
        return EventoResponse.de(repo.save(e));
    }

    @Transactional
    public void excluir(UUID id, UUID dono) {
        repo.delete(doDono(id, dono));
    }

    /** Evento do dono ou 404 — não vaza existência de evento de outro usuário. */
    private Evento doDono(UUID id, UUID dono) {
        return repo.findById(id)
                .filter(e -> e.getCriadoPor().equals(dono))
                .orElseThrow(EventoNaoEncontradoException::new);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarDoUsuario(UUID usuarioId) {
        return repo.findByCriadoPorOrderByInicioAsc(usuarioId)
                .stream()
                .map(EventoResponse::de)
                .toList();
    }
}
