package io.lpsoft.features.recorrencia;

import io.lpsoft.features.recorrencia.RecorrenciaExceptions.EventoNaoEncontrado;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaJaExiste;
import io.lpsoft.features.recorrencia.RecorrenciaExceptions.RecorrenciaNaoEncontrada;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.CriarRecorrencia;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.ProcessamentoResponse;
import io.lpsoft.features.recorrencia.dto.RecorrenciaDtos.RecorrenciaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
public class RecorrenciaController {

    private final RecorrenciaService service;

    @PostMapping("eventos/{eventoId}/recorrencia")
    public ResponseEntity<RecorrenciaResponse> registrar(
            @PathVariable UUID eventoId, @Valid @RequestBody CriarRecorrencia req) {
        var r = service.registrar(eventoId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(RecorrenciaResponse.de(r));
    }

    @GetMapping("eventos/{eventoId}/recorrencia")
    public List<RecorrenciaResponse> listar(@PathVariable UUID eventoId) {
        return service.listar(eventoId).stream().map(RecorrenciaResponse::de).toList();
    }

    @DeleteMapping("eventos/{eventoId}/recorrencia")
    public ResponseEntity<Void> desativar(@PathVariable UUID eventoId) {
        service.desativar(eventoId);
        return ResponseEntity.noContent().build();
    }

    /** Trigger manual — útil para demo/validação sem esperar o job. */
    @PostMapping("recorrencia/processar")
    public ProcessamentoResponse processar() {
        return new ProcessamentoResponse(service.processarPendentes());
    }

    @ExceptionHandler({EventoNaoEncontrado.class, RecorrenciaNaoEncontrada.class})
    public ResponseEntity<Map<String, String>> naoEncontrado(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(RecorrenciaJaExiste.class)
    public ResponseEntity<Map<String, String>> jaExiste(RecorrenciaJaExiste e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }
}
