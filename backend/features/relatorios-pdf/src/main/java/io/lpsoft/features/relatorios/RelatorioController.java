package io.lpsoft.features.relatorios;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService service;

    /** Prévia do relatório (mesmo conteúdo do PDF, em texto). */
    @GetMapping("eventos")
    public List<String> eventosPreview() {
        return service.linhasRelatorio();
    }

    @GetMapping("eventos.pdf")
    public ResponseEntity<byte[]> eventosPdf() {
        byte[] pdf = service.eventosPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"eventos.pdf\"")
                .body(pdf);
    }
}
