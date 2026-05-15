package io.lpsoft.features.recorrencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventoRecorrenciaRepository extends JpaRepository<EventoRecorrencia, UUID> {

    List<EventoRecorrencia> findByAtivoTrueAndProximoDisparoLessThanEqual(Instant momento);

    List<EventoRecorrencia> findByEventoModeloId(UUID eventoModeloId);

    boolean existsByEventoModeloIdAndAtivoTrue(UUID eventoModeloId);
}
