package io.lpsoft.features.categorias;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Categoria> findByNomeIgnoreCase(String nome);
}
