package io.lpsoft.features.categorias;

import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.features.categorias.CategoriaExceptions.CategoriaNaoEncontrada;
import io.lpsoft.features.categorias.CategoriaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.categorias.CategoriaExceptions.NomeCategoriaDuplicado;
import io.lpsoft.features.categorias.dto.CategoriaDtos.CriarCategoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock CategoriaRepository categorias;
    @Mock EventoCategoriaRepository vinculos;
    @Mock EventoRepository eventos;
    @InjectMocks CategoriaService service;

    @Test
    void deve_criar_categoria_quando_nome_unico() {
        when(categorias.existsByNomeIgnoreCase("Trabalho")).thenReturn(false);
        when(categorias.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = service.criar(new CriarCategoria("Trabalho", "#ff0000"));

        assertThat(resp.nome()).isEqualTo("Trabalho");
        assertThat(resp.cor()).isEqualTo("#ff0000");
    }

    @Test
    void deve_rejeitar_categoria_com_nome_duplicado() {
        when(categorias.existsByNomeIgnoreCase("Trabalho")).thenReturn(true);

        assertThatThrownBy(() -> service.criar(new CriarCategoria("Trabalho", null)))
                .isInstanceOf(NomeCategoriaDuplicado.class);
        verify(categorias, never()).save(any());
    }

    @Test
    void deve_rejeitar_atribuicao_quando_evento_nao_existe() {
        UUID eventoId = UUID.randomUUID();
        when(eventos.existsById(eventoId)).thenReturn(false);

        assertThatThrownBy(() -> service.atribuir(eventoId, UUID.randomUUID()))
                .isInstanceOf(EventoNaoEncontrado.class);
        verify(vinculos, never()).save(any());
    }

    @Test
    void deve_rejeitar_atribuicao_quando_categoria_nao_existe() {
        UUID eventoId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();
        when(eventos.existsById(eventoId)).thenReturn(true);
        when(categorias.existsById(categoriaId)).thenReturn(false);

        assertThatThrownBy(() -> service.atribuir(eventoId, categoriaId))
                .isInstanceOf(CategoriaNaoEncontrada.class);
        verify(vinculos, never()).save(any());
    }

    @Test
    void deve_atribuir_quando_evento_e_categoria_existem() {
        UUID eventoId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();
        when(eventos.existsById(eventoId)).thenReturn(true);
        when(categorias.existsById(categoriaId)).thenReturn(true);

        service.atribuir(eventoId, categoriaId);

        verify(vinculos).save(any(EventoCategoria.class));
    }
}
