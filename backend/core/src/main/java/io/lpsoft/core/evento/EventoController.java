package io.lpsoft.core.evento;

import io.lpsoft.core.auth.PrincipalUsuario;
import io.lpsoft.core.evento.dto.CriarEventoRequest;
import io.lpsoft.core.evento.dto.EventoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService service;

    @PostMapping
    public ResponseEntity<EventoResponse> criar(
            @Valid @RequestBody CriarEventoRequest req,
            @AuthenticationPrincipal PrincipalUsuario principal
    ) {
        EventoResponse resp = service.criar(req, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public List<EventoResponse> listar(@AuthenticationPrincipal PrincipalUsuario principal) {
        return service.listarDoUsuario(principal.getId());
    }

    @ExceptionHandler(EventoInvalidoException.class)
    public ResponseEntity<Map<String, String>> invalido(EventoInvalidoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
    }
}
