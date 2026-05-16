package io.lpsoft.features.recorrencia;

public final class RecorrenciaExceptions {

    private RecorrenciaExceptions() {}

    public static class EventoNaoEncontrado extends RuntimeException {
        public EventoNaoEncontrado() {
            super("Evento não encontrado");
        }
    }

    public static class RecorrenciaJaExiste extends RuntimeException {
        public RecorrenciaJaExiste() {
            super("Já existe uma recorrência ativa para este evento");
        }
    }

    public static class RecorrenciaNaoEncontrada extends RuntimeException {
        public RecorrenciaNaoEncontrada() {
            super("Recorrência não encontrada");
        }
    }
}
