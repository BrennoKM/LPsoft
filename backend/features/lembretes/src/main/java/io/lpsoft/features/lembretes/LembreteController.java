package io.lpsoft.features.lembretes;

import io.lpsoft.features.lembretes.dto.LembreteDtos.AtualizarPoliticaRequest;
import io.lpsoft.features.lembretes.dto.LembreteDtos.LembreteAgendadoResponse;
import io.lpsoft.features.lembretes.dto.LembreteDtos.PoliticaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lembretes")
@RequiredArgsConstructor
public class LembreteController {

    private final PoliticaLembrete politica;
    private final AgendadorLembrete agendador;

    @GetMapping("politica")
    public PoliticaResponse politica() {
        return new PoliticaResponse(politica.antecedenciaHoras());
    }

    @PutMapping("politica")
    public PoliticaResponse atualizar(@RequestBody AtualizarPoliticaRequest req) {
        if (req == null || req.antecedenciaHoras() == null) {
            throw new IllegalArgumentException("antecedenciaHoras é obrigatório");
        }
        politica.definirHoras(req.antecedenciaHoras());
        return new PoliticaResponse(politica.antecedenciaHoras());
    }

    @GetMapping("agendados")
    public List<LembreteAgendadoResponse> agendados() {
        return agendador.lembretesAgendados().stream()
                .map(LembreteAgendadoResponse::de)
                .toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> aoFalharValidacao(IllegalArgumentException e) {
        return Map.of("erro", e.getMessage());
    }
}
