package io.lpsoft.features.lembretes;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Política de antecedência dos lembretes — o "miolo" visível desta feature.
 * Define quanto tempo antes do início do evento o titular deve ser avisado.
 *
 * Mantida em memória (PoC); numa implementação real seria persistida, poss
 * ivelmente por titular. O default é 24h. Configurável em runtime pela tela
 * de Lembretes, sem rebuild.
 */
@Component
public class PoliticaLembrete {

    private static final Duration PADRAO = Duration.ofHours(24);
    private static final long MIN_HORAS = 1;
    private static final long MAX_HORAS = 24 * 30; // 30 dias

    private volatile Duration antecedencia = PADRAO;

    public Duration antecedencia() {
        return antecedencia;
    }

    public long antecedenciaHoras() {
        return antecedencia.toHours();
    }

    /** Atualiza a antecedência (em horas). Fora da faixa → IllegalArgumentException. */
    public void definirHoras(long horas) {
        if (horas < MIN_HORAS || horas > MAX_HORAS) {
            throw new IllegalArgumentException(
                    "antecedência deve estar entre " + MIN_HORAS + " e " + MAX_HORAS + " horas");
        }
        this.antecedencia = Duration.ofHours(horas);
    }
}
