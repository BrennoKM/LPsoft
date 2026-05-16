package io.lpsoft.core.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

    List<Evento> findByCriadoPorOrderByInicioAsc(UUID criadoPor);
}
