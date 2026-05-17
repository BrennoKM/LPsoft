package io.lpsoft.features.resumocategoria;

import io.lpsoft.features.categorias.Categoria;
import io.lpsoft.features.categorias.CategoriaRepository;
import io.lpsoft.features.categorias.EventoCategoria;
import io.lpsoft.features.categorias.EventoCategoriaRepository;
import io.lpsoft.features.resumocategoria.dto.ResumoCategoriaDtos.ResumoItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumoCategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock EventoCategoriaRepository eventoCategoriaRepository;
    @InjectMocks ResumoCategoriaService service;

    private Categoria categoria(UUID id, String nome) {
        return Categoria.builder().id(id).nome(nome).cor("#123456").criadoEm(Instant.now()).build();
    }

    private EventoCategoria vinculo(UUID eventoId, UUID categoriaId) {
        return new EventoCategoria(new EventoCategoria.Id(eventoId, categoriaId));
    }

    @Test
    void conta_eventos_por_categoria_e_inclui_categoria_sem_eventos() {
        UUID catA = UUID.randomUUID();
        UUID catB = UUID.randomUUID();
        when(categoriaRepository.findAll()).thenReturn(List.of(
                categoria(catA, "Alfa"), categoria(catB, "Beta")));
        when(eventoCategoriaRepository.findAll()).thenReturn(List.of(
                vinculo(UUID.randomUUID(), catA),
                vinculo(UUID.randomUUID(), catA)));

        List<ResumoItem> resumo = service.resumo();

        assertThat(resumo).extracting(ResumoItem::nome, ResumoItem::totalEventos)
                .containsExactly(
                        org.assertj.core.api.Assertions.tuple("Alfa", 2L),
                        org.assertj.core.api.Assertions.tuple("Beta", 0L));
    }

    @Test
    void ordena_por_nome_ignorando_caixa() {
        when(categoriaRepository.findAll()).thenReturn(List.of(
                categoria(UUID.randomUUID(), "zebra"),
                categoria(UUID.randomUUID(), "Abacate")));
        when(eventoCategoriaRepository.findAll()).thenReturn(List.of());

        assertThat(service.resumo()).extracting(ResumoItem::nome)
                .containsExactly("Abacate", "zebra");
    }
}
