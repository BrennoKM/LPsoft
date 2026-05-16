package io.lpsoft.features.analytics;

import io.lpsoft.features.analytics.dto.AnalyticsDtos.PorDia;
import io.lpsoft.features.analytics.dto.AnalyticsDtos.PorUsuario;
import io.lpsoft.features.analytics.dto.AnalyticsDtos.ResumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EventoDiarioRepository repo;

    @Transactional(readOnly = true)
    public ResumoResponse resumo() {
        List<EventoDiario> registros = repo.findAll();

        long total = registros.stream().mapToLong(EventoDiario::getTotal).sum();

        Map<java.util.UUID, Long> porUsuarioMap = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getId().getCriadoPor(),
                        Collectors.summingLong(EventoDiario::getTotal)));

        List<PorUsuario> porUsuario = porUsuarioMap.entrySet().stream()
                .map(e -> new PorUsuario(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(PorUsuario::total).reversed())
                .toList();

        List<PorDia> porDia = registros.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getId().getDia(),
                        Collectors.summingLong(EventoDiario::getTotal)))
                .entrySet().stream()
                .map(e -> new PorDia(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(PorDia::dia))
                .toList();

        return new ResumoResponse(total, porUsuario, porDia);
    }
}
