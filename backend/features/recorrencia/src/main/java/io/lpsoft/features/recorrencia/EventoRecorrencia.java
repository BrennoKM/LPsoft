package io.lpsoft.features.recorrencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "evento_recorrencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoRecorrencia {

    @Id
    private UUID id;

    /** FK informal para evento(id) do core — sem constraint física. */
    @Column(name = "evento_modelo_id", nullable = false)
    private UUID eventoModeloId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Frequencia freq;

    @Column(nullable = false)
    private int intervalo;

    @Column(name = "proximo_disparo", nullable = false)
    private Instant proximoDisparo;

    @Column
    private Instant ate;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}
