package io.lpsoft.core.evento;

import io.lpsoft.core.shared.events.EventoCriado;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.event.EventListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@TestComponent
public class EventoCriadoCapturador {

    private final List<EventoCriado> recebidos = new CopyOnWriteArrayList<>();

    @EventListener
    public void on(EventoCriado evento) {
        recebidos.add(evento);
    }

    public List<EventoCriado> recebidos() {
        return Collections.unmodifiableList(recebidos);
    }

    public void limpar() {
        recebidos.clear();
    }
}
