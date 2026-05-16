package io.lpsoft.features.analytics;

import io.lpsoft.features.analytics.dto.AnalyticsDtos.ResumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("resumo")
    public ResumoResponse resumo() {
        return service.resumo();
    }
}
