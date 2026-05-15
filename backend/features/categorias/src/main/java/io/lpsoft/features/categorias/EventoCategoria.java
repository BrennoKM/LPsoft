package io.lpsoft.features.categorias;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Associação evento↔categoria. evento_id é um UUID solto (sem FK ao core),
 * por design de fronteira de feature.
 */
@Entity
@Table(name = "evento_categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoCategoria {

    @EmbeddedId
    private Id id;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        @Column(name = "evento_id", nullable = false)
        private UUID eventoId;

        @Column(name = "categoria_id", nullable = false)
        private UUID categoriaId;
    }
}
