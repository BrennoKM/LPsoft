package io.lpsoft.features.notificacao;

import io.lpsoft.features.notificacao.dto.NotificacaoDtos.NotificacaoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificacao")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoRegistry registry;

    @GetMapping("programadas")
    public List<NotificacaoResponse> programadas() {
        return registry.todas().stream().map(NotificacaoResponse::de).toList();
    }
}
