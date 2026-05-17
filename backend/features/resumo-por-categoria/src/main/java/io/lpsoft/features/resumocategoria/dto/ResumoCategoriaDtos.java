package io.lpsoft.features.resumocategoria.dto;

import java.util.UUID;

public final class ResumoCategoriaDtos {

    private ResumoCategoriaDtos() {}

    /** Quantos eventos estão associados a esta categoria. */
    public record ResumoItem(UUID categoriaId, String nome, String cor, long totalEventos) {}
}
