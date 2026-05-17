package io.lpsoft.core.evento;

public class EventoNaoEncontradoException extends RuntimeException {
    public EventoNaoEncontradoException() {
        super("Evento não encontrado");
    }
}
