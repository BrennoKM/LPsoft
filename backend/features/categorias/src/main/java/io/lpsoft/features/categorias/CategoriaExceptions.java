package io.lpsoft.features.categorias;

public final class CategoriaExceptions {

    private CategoriaExceptions() {}

    public static class CategoriaNaoEncontrada extends RuntimeException {
        public CategoriaNaoEncontrada() {
            super("Categoria não encontrada");
        }
    }

    public static class EventoNaoEncontrado extends RuntimeException {
        public EventoNaoEncontrado() {
            super("Evento não encontrado");
        }
    }

    public static class NomeCategoriaDuplicado extends RuntimeException {
        public NomeCategoriaDuplicado(String nome) {
            super("Categoria já existe: " + nome);
        }
    }
}
