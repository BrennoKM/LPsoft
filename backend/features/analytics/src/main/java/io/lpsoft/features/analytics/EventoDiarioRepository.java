package io.lpsoft.features.analytics;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoDiarioRepository extends JpaRepository<EventoDiario, EventoDiario.Id> {
}
