package io.lpsoft.features.notificacao;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Guarda em memória as notificações (simulado). Cada item nasce PROGRAMADA
 * (agendada para o futuro) e passa a ENVIADA quando o {@link NotificacaoDispatcher}
 * a despacha — ao chegar {@code programadaPara}. Numa implementação real o
 * dispatcher faria o toast/in-app de fato (WebSocket, push, …).
 */
@Component
public class NotificacaoRegistry {

    /** Notificação com estado: enviadaEm == null enquanto apenas programada. */
    public static final class NotificacaoProgramada {
        private final UUID eventoId;
        private final String titulo;
        private final Instant programadaPara;
        private volatile Instant enviadaEm;

        NotificacaoProgramada(UUID eventoId, String titulo, Instant programadaPara) {
            this.eventoId = eventoId;
            this.titulo = titulo;
            this.programadaPara = programadaPara;
        }

        public UUID eventoId() { return eventoId; }
        public String titulo() { return titulo; }
        public Instant programadaPara() { return programadaPara; }
        public Instant enviadaEm() { return enviadaEm; }
        public boolean enviada() { return enviadaEm != null; }
    }

    private final List<NotificacaoProgramada> notificacoes = new CopyOnWriteArrayList<>();

    public void registrar(UUID eventoId, String titulo, Instant programadaPara) {
        notificacoes.add(new NotificacaoProgramada(eventoId, titulo, programadaPara));
    }

    public List<NotificacaoProgramada> todas() {
        return List.copyOf(notificacoes);
    }

    /**
     * Marca como enviadas todas as pendentes cuja hora já chegou.
     * Retorna as que acabaram de ser despachadas (para log/efeito).
     */
    public List<NotificacaoProgramada> despacharAte(Instant agora) {
        List<NotificacaoProgramada> despachadas = new ArrayList<>();
        for (NotificacaoProgramada n : notificacoes) {
            if (!n.enviada() && !n.programadaPara().isAfter(agora)) {
                n.enviadaEm = agora;
                despachadas.add(n);
            }
        }
        return despachadas;
    }
}
