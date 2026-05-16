package io.lpsoft.features.categorias;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(length = 7)
    private String cor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;
}
