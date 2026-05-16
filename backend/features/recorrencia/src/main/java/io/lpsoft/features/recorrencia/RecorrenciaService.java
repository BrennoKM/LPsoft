package io.lpsoft.features.recorrencia;

import io.lpsoft.core.evento.Evento;
import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.core.evento.EventoService;
import io.lpsoft.core.evento.dto.CriarEventoRequest;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaJaExiste;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaNaoEncontrada;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.CriarRecorrencia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class RecorrenciaService {

    private final EventoRecorrenciaRepository repo;
    // Feature consome o core diretamente (feature→core é permitido).
    private final EventoRepository eventos;
    private final EventoService eventoService;
    /** Quantas ocorrências materializar de imediato ao registrar. */
    private final int janela;

    public RecorrenciaService(
            EventoRecorrenciaRepository repo,
            EventoRepository eventos,
            EventoService eventoService,
            @Value("${features.recorrencia.janela:5}") int janela) {
        this.repo = repo;
        this.eventos = eventos;
        this.eventoService = eventoService;
        this.janela = janela;
    }

    @Transactional
    public EventoRecorrencia registrar(UUID eventoModeloId, CriarRecorrencia req) {
        Evento modelo = eventos.findById(eventoModeloId).orElseThrow(EventoNaoEncontrado::new);
        if (repo.existsByEventoModeloIdAndAtivoTrue(eventoModeloId)) {
            throw new RecorrenciaJaExiste();
        }
        // Uma recorrência nova não cria instâncias no passado: pula o
        // primeiro disparo para a primeira data >= agora (como um calendário
        // real). Sem isso, modelo antigo geraria eventos históricos e o job
        // entupiria a agenda repondo o passado.
        Instant agora = Instant.now();
        Instant primeiro = req.freq().avancar(modelo.getInicio(), req.intervalo());
        while (primeiro.isBefore(agora)) {
            primeiro = req.freq().avancar(primeiro, req.intervalo());
        }
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
        repo.save(r);

        // Materializa a janela inicial AGORA: marcou "repete" → as próximas
        // ocorrências aparecem na hora (e lembretes/analytics reagem a cada
        // uma). O job continua repondo conforme o tempo passa.
        int criadas = 0;
        for (int i = 0; i < janela; i++) {
            if (!tentarGerar(r, modelo)) break;
            criadas++;
        }
        log.info("Recorrência registrada: {} ocorrência(s) materializada(s) na janela", criadas);
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
     * Reposição contínua: processa regras vencidas, criando a próxima
     * ocorrência no core e avançando o disparo conforme o tempo passa. Regra
     * órfã (evento modelo apagado — consequência da FK informal) é desativada.
     * Retorna quantas ocorrências foram criadas.
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
            if (tentarGerar(r, modelo)) {
                criadas++;
            }
        }
        repo.saveAll(pendentes);
        if (criadas > 0) {
            log.info("Recorrência: {} ocorrência(s) criada(s)", criadas);
        }
        return criadas;
    }

    /**
     * Cria UMA ocorrência no disparo atual e avança o ponteiro. Devolve false
     * (sem criar) se a regra já está inativa ou se o disparo passou de
     * {@code ate} — desativando a regra nesse caso.
     */
    private boolean tentarGerar(EventoRecorrencia r, Evento modelo) {
        if (!r.isAtivo()) {
            return false;
        }
        if (r.getAte() != null && r.getProximoDisparo().isAfter(r.getAte())) {
            r.setAtivo(false);
            return false;
        }
        Duration duracao = Duration.between(modelo.getInicio(), modelo.getFim());
        Instant inicio = r.getProximoDisparo();
        eventoService.criar(
                new CriarEventoRequest(modelo.getTitulo(), modelo.getDescricao(), inicio, inicio.plus(duracao)),
                modelo.getCriadoPor()
        );
        Instant proximo = r.getFreq().avancar(r.getProximoDisparo(), r.getIntervalo());
        if (r.getAte() != null && proximo.isAfter(r.getAte())) {
            r.setAtivo(false);
        } else {
            r.setProximoDisparo(proximo);
        }
        return true;
    }
}
