package io.lpsoft.features.relatorios;

import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.core.shared.spi.SecaoRelatorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock EventoRepository eventos;

    private String texto(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    @Test
    void sem_secoes_gera_pdf_apenas_com_o_core() {
        when(eventos.count()).thenReturn(7L);
        var service = new RelatorioService(eventos, List.of());

        byte[] pdf = service.eventosPdf();

        String txt = texto(pdf);
        assertThat(txt).startsWith("%PDF-1.4");
        assertThat(txt).contains("Total de eventos cadastrados: 7");
        assertThat(txt).doesNotContain("== Analytics ==");
    }

    @Test
    void com_secao_de_analytics_inclui_o_bloco_opcional() {
        when(eventos.count()).thenReturn(3L);
        SecaoRelatorio analytics = new SecaoRelatorio() {
            @Override
            public String titulo() {
                return "Analytics";
            }

            @Override
            public List<String> linhas() {
                return List.of("Total de eventos contabilizados: 3", "2026-08-01: 2");
            }
        };
        var service = new RelatorioService(eventos, List.of(analytics));

        String txt = texto(service.eventosPdf());

        assertThat(txt).contains("Total de eventos cadastrados: 3");
        assertThat(txt).contains("== Analytics ==");
        assertThat(txt).contains("2026-08-01: 2");
    }
}
