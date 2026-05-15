package io.lpsoft.features.recorrencia;

import io.lpsoft.core.evento.Evento;
import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.core.evento.EventoService;
import io.lpsoft.core.evento.dto.CriarEventoRequest;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaJaExiste;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaNaoEncontrada;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.CriarRecorrencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecorrenciaService {

    private final EventoRecorrenciaRepository repo;
    // Feature consome o core diretamente (feature→core é permitido).
    private final EventoRepository eventos;
    private final EventoService eventoService;

    @Transactional
    public EventoRecorrencia registrar(UUID eventoModeloId, CriarRecorrencia req) {
        Evento modelo = eventos.findById(eventoModeloId).orElseThrow(EventoNaoEncontrado::new);
        if (repo.existsByEventoModeloIdAndAtivoTrue(eventoModeloId)) {
            throw new RecorrenciaJaExiste();
        }
        Instant primeiro = req.freq().avancar(modelo.getInicio(), req.intervalo());
        EventoRecorrencia r = EventoRecorrencia.builder()
                .id(UUID.randomUUID())
                .eventoModeloId(eventoModeloId)
                .freq(req.freq())
                .intervalo(req.intervalo())
                .proximoDisparo(primeiro)
                .ate(req.ate())
                .ativo(true)
                .criadoEm(Instant.now())
                .build();
        return repo.save(r);
    }

    @Transactional(readOnly = true)
    public List<EventoRecorrencia> listar(UUID eventoModeloId) {
        return repo.findByEventoModeloId(eventoModeloId);
    }

    @Transactional
    public void desativar(UUID eventoModeloId) {
        List<EventoRecorrencia> regras = repo.findByEventoModeloId(eventoModeloId);
        if (regras.isEmpty()) {
            throw new RecorrenciaNaoEncontrada();
        }
        regras.forEach(r -> r.setAtivo(false));
        repo.saveAll(regras);
    }

    /**
     * Processa regras vencidas: cria a próxima ocorrência no core (via
     * EventoService) e avança o disparo. Regra órfã (evento modelo apagado —
     * consequência da FK informal) é desativada. Retorna quantas ocorrências
     * foram criadas.
     */
    @Transactional
    public int processarPendentes() {
        Instant agora = Instant.now();
        List<EventoRecorrencia> pendentes = repo.findByAtivoTrueAndProximoDisparoLessThanEqual(agora);
        int criadas = 0;

        for (EventoRecorrencia r : pendentes) {
            Evento modelo = eventos.findById(r.getEventoModeloId()).orElse(null);
            if (modelo == null) {
                log.warn("Recorrência {} órfã (evento modelo {} não existe) — desativando",
                        r.getId(), r.getEventoModeloId());
                r.setAtivo(false);
                continue;
            }

            Duration duracao = Duration.between(modelo.getInicio(), modelo.getFim());
            Instant inicio = r.getProximoDisparo();
            eventoService.criar(
                    new CriarEventoRequest(modelo.getTitulo(), modelo.getDescricao(), inicio, inicio.plus(duracao)),
                    modelo.getCriadoPor()
            );
            criadas++;

            Instant proximo = r.getFreq().avancar(r.getProximoDisparo(), r.getIntervalo());
            if (r.getAte() != null && proximo.isAfter(r.getAte())) {
                r.setAtivo(false);
            } else {
                r.setProximoDisparo(proximo);
            }
        }
        repo.saveAll(pendentes);
        if (criadas > 0) {
            log.info("Recorrência: {} ocorrência(s) criada(s)", criadas);
        }
        return criadas;
    }
}
