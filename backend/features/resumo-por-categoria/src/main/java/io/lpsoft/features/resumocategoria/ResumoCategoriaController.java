package io.lpsoft.features.resumocategoria;

import io.lpsoft.features.resumocategoria.dto.ResumoCategoriaDtos.ResumoItem;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resumo-categoria")
@RequiredArgsConstructor
public class ResumoCategoriaController {

    private final ResumoCategoriaService service;

    @GetMapping
    public List<ResumoItem> resumo() {
        return service.resumo();
    }
}
