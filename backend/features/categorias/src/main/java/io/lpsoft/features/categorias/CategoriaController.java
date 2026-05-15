package io.lpsoft.features.categorias;

import io.lpsoft.features.categorias.CategoriaExceptions.CategoriaNaoEncontrada;
import io.lpsoft.features.categorias.CategoriaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.categorias.CategoriaExceptions.NomeCategoriaDuplicado;
import io.lpsoft.features.categorias.dto.CategoriaDtos.AtribuirCategoria;
import io.lpsoft.features.categorias.dto.CategoriaDtos.AtualizarCategoria;
import io.lpsoft.features.categorias.dto.CategoriaDtos.CategoriaResponse;
import io.lpsoft.features.categorias.dto.CategoriaDtos.CriarCategoria;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService service;

    @PostMapping("categorias")
    public ResponseEntity<CategoriaResponse> criar(@Valid @RequestBody CriarCategoria req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(req));
    }

    @GetMapping("categorias")
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @PatchMapping("categorias/{id}")
    public CategoriaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarCategoria req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("categorias/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("eventos/{eventoId}/categorias")
    public List<CategoriaResponse> doEvento(@PathVariable UUID eventoId) {
        return service.listarDoEvento(eventoId);
    }

    @PostMapping("eventos/{eventoId}/categorias")
    public ResponseEntity<Void> atribuir(@PathVariable UUID eventoId, @Valid @RequestBody AtribuirCategoria req) {
        service.atribuir(eventoId, req.categoriaId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("eventos/{eventoId}/categorias/{categoriaId}")
    public ResponseEntity<Void> remover(@PathVariable UUID eventoId, @PathVariable UUID categoriaId) {
        service.remover(eventoId, categoriaId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NomeCategoriaDuplicado.class)
    public ResponseEntity<Map<String, String>> duplicado(NomeCategoriaDuplicado e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler({CategoriaNaoEncontrada.class, EventoNaoEncontrado.class})
    public ResponseEntity<Map<String, String>> naoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }
}
