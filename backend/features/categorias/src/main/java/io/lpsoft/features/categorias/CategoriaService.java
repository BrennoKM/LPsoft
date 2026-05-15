package io.lpsoft.features.categorias;

import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.features.categorias.CategoriaExceptions.CategoriaNaoEncontrada;
import io.lpsoft.features.categorias.CategoriaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.categorias.CategoriaExceptions.NomeCategoriaDuplicado;
import io.lpsoft.features.categorias.dto.CategoriaDtos.AtualizarCategoria;
import io.lpsoft.features.categorias.dto.CategoriaDtos.CategoriaResponse;
import io.lpsoft.features.categorias.dto.CategoriaDtos.CriarCategoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categorias;
    private final EventoCategoriaRepository vinculos;
    // Feature consome o core (feature→core é permitido). Core nunca consome a feature.
    private final EventoRepository eventos;

    @Transactional
    public CategoriaResponse criar(CriarCategoria req) {
        if (categorias.existsByNomeIgnoreCase(req.nome())) {
            throw new NomeCategoriaDuplicado(req.nome());
        }
        Categoria c = Categoria.builder()
                .id(UUID.randomUUID())
                .nome(req.nome())
                .cor(req.cor())
                .criadoEm(Instant.now())
                .build();
        return CategoriaResponse.de(categorias.save(c));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAll().stream().map(CategoriaResponse::de).toList();
    }

    @Transactional
    public CategoriaResponse atualizar(UUID id, AtualizarCategoria req) {
        Categoria c = categorias.findById(id).orElseThrow(CategoriaNaoEncontrada::new);
        if (!c.getNome().equalsIgnoreCase(req.nome()) && categorias.existsByNomeIgnoreCase(req.nome())) {
            throw new NomeCategoriaDuplicado(req.nome());
        }
        c.setNome(req.nome());
        c.setCor(req.cor());
        return CategoriaResponse.de(categorias.save(c));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!categorias.existsById(id)) {
            throw new CategoriaNaoEncontrada();
        }
        categorias.deleteById(id);
    }

    @Transactional
    public void atribuir(UUID eventoId, UUID categoriaId) {
        if (!eventos.existsById(eventoId)) {
            throw new EventoNaoEncontrado();
        }
        if (!categorias.existsById(categoriaId)) {
            throw new CategoriaNaoEncontrada();
        }
        vinculos.save(new EventoCategoria(new EventoCategoria.Id(eventoId, categoriaId)));
    }

    @Transactional
    public void remover(UUID eventoId, UUID categoriaId) {
        vinculos.deleteByIdEventoIdAndIdCategoriaId(eventoId, categoriaId);
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarDoEvento(UUID eventoId) {
        return vinculos.findByIdEventoId(eventoId).stream()
                .map(v -> categorias.findById(v.getId().getCategoriaId()).orElse(null))
                .filter(c -> c != null)
                .map(CategoriaResponse::de)
                .toList();
    }
}
