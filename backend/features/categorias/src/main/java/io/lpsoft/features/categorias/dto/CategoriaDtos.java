package io.lpsoft.features.categorias.dto;

import io.lpsoft.features.categorias.Categoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class CategoriaDtos {

    private CategoriaDtos() {}

    public record CriarCategoria(
            @NotBlank @Size(max = 80) String nome,
            @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "cor deve ser hex #RRGGBB") String cor
    ) {}

    public record AtualizarCategoria(
            @NotBlank @Size(max = 80) String nome,
            @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "cor deve ser hex #RRGGBB") String cor
    ) {}

    public record AtribuirCategoria(UUID categoriaId) {}

    public record CategoriaResponse(UUID id, String nome, String cor, Instant criadoEm) {
        public static CategoriaResponse de(Categoria c) {
            return new CategoriaResponse(c.getId(), c.getNome(), c.getCor(), c.getCriadoEm());
        }
    }
}
