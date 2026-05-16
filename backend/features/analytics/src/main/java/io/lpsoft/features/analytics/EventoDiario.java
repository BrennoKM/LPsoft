package io.lpsoft.features.analytics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Agregado: total de eventos de um usuário em um dia (dia = data de início do
 * evento, em UTC). criado_por é um UUID solto, sem FK ao core — fronteira de
 * feature, igual às demais.
 */
@Entity
@Table(name = "analytics_evento_diario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoDiario {

    @EmbeddedId
    private Id id;

    @Column(nullable = false)
    private int total;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {
        @Column(name = "criado_por", nullable = false)
        private UUID criadoPor;

        @Column(name = "dia", nullable = false)
        private LocalDate dia;
    }
}
