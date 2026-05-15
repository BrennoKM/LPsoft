package io.lpsoft.features.recorrencia;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga o agendamento do Spring SOMENTE quando esta feature está no build.
 * Sem o módulo no classpath, não há @EnableScheduling — o app não cria
 * o scheduler, e o RecorrenciaJob não existe. Outra peça do corte de feature.
 */
@Configuration
@EnableScheduling
public class RecorrenciaSchedulingConfig {
}
