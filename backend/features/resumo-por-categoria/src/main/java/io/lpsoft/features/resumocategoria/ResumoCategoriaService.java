package io.lpsoft.features.resumocategoria;

import io.lpsoft.features.categorias.Categoria;
import io.lpsoft.features.categorias.CategoriaRepository;
import io.lpsoft.features.categorias.EventoCategoria;
import io.lpsoft.features.categorias.EventoCategoriaRepository;
import io.lpsoft.features.resumocategoria.dto.ResumoCategoriaDtos.ResumoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Conta quantos eventos estão associados a cada categoria. Importa tipos da
 * feature 'categorias' (Categoria, EventoCategoria{,Repository}) — é a
 * dependência ESTRITA: este código não compila sem o módulo 'categorias'.
 */
@Service
@RequiredArgsConstructor
public class ResumoCategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final EventoCategoriaRepository eventoCategoriaRepository;

    public List<ResumoItem> resumo() {
        Map<UUID, Long> totalPorCategoria = eventoCategoriaRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        (EventoCategoria ec) -> ec.getId().getCategoriaId(),
                        Collectors.counting()));

        return categoriaRepository.findAll().stream()
                .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                .map((Categoria c) -> new ResumoItem(
                        c.getId(),
                        c.getNome(),
                        c.getCor(),
                        totalPorCategoria.getOrDefault(c.getId(), 0L)))
                .toList();
    }
}
