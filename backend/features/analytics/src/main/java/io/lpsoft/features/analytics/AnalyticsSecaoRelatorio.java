package io.lpsoft.features.analytics;

import io.lpsoft.core.shared.spi.SecaoRelatorio;
import io.lpsoft.features.analytics.dto.AnalyticsDtos.ResumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter do SPI {@link SecaoRelatorio}: existe só quando o módulo 'analytics'
 * está no build. Consumidores (ex.: relatorios-pdf) recebem esta seção sem
 * depender de 'analytics' em tempo de compilação.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsSecaoRelatorio implements SecaoRelatorio {

    private final AnalyticsService service;

    @Override
    public String titulo() {
        return "Analytics";
    }

    @Override
    public List<String> linhas() {
        ResumoResponse r = service.resumo();
        List<String> linhas = new ArrayList<>();
        linhas.add("Total de eventos contabilizados: " + r.total());
        r.porDia().forEach(d -> linhas.add(d.dia() + ": " + d.total()));
        return linhas;
    }
}
