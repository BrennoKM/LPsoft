package io.lpsoft.core.evento;

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
@Table(name = "evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private Instant inicio;

    @Column(nullable = false)
    private Instant fim;

    @Column(name = "criado_por", nullable = false)
    private UUID criadoPor;

    /** Raiz da qual este evento deriva (ex.: ocorrência → modelo). Null se criado direto. */
    @Column(name = "origem_id")
    private UUID origemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventoStatus status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;
}
