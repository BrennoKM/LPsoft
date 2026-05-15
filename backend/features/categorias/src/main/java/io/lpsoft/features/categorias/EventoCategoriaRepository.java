package io.lpsoft.features.categorias;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoCategoriaRepository extends JpaRepository<EventoCategoria, EventoCategoria.Id> {

    List<EventoCategoria> findByIdEventoId(UUID eventoId);

    void deleteByIdEventoIdAndIdCategoriaId(UUID eventoId, UUID categoriaId);
}
