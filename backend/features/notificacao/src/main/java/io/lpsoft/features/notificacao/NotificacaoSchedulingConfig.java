package io.lpsoft.features.notificacao;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Liga o agendamento do Spring SOMENTE quando esta feature está no build.
 * Sem o módulo no classpath não há @EnableScheduling nem NotificacaoDispatcher
 * — outra peça do corte de feature.
 */
@Configuration
@EnableScheduling
public class NotificacaoSchedulingConfig {
}
