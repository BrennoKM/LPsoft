package io.lpsoft.features.relatorios;

import io.lpsoft.core.evento.EventoRepository;
import io.lpsoft.core.shared.spi.SecaoRelatorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta o relatório. A seção do core sempre existe; as demais vêm de
 * {@link SecaoRelatorio} — Spring injeta todas as implementações no classpath.
 * Sem 'analytics' no build, a lista chega vazia e o relatório sai só com o
 * core: integração OPCIONAL, sem dependência Maven entre as features.
 */
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final EventoRepository eventos;
    private final List<SecaoRelatorio> secoes;

    /**
     * Conteúdo do relatório (mesmo modelo usado para o PDF e para a prévia).
     * A seção do core sempre existe; as demais vêm do SPI {@link SecaoRelatorio}.
     */
    @Transactional(readOnly = true)
    public List<String> linhasRelatorio() {
        List<String> linhas = new ArrayList<>();
        linhas.add("Relatorio de eventos");
        linhas.add("Gerado em: " + LocalDate.now());
        linhas.add("");
        linhas.add("Total de eventos cadastrados: " + eventos.count());

        for (SecaoRelatorio secao : secoes) {
            linhas.add("");
            linhas.add("== " + secao.titulo() + " ==");
            linhas.addAll(secao.linhas());
        }
        return linhas;
    }

    public byte[] eventosPdf() {
        return MiniPdf.gerar(linhasRelatorio());
    }
}
