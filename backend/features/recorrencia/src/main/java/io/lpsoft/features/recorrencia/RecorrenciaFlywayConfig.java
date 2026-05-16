package io.lpsoft.features.recorrencia;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Anexa a location de migrations da feature. Bean só existe quando o módulo
 * está no classpath — sem ele, a migration de recorrência nunca roda.
 */
@Configuration
public class RecorrenciaFlywayConfig {

    private static final String LOCATION = "classpath:db/migration/features/recorrencia";

    @Bean
    public FlywayConfigurationCustomizer recorrenciaFlywayLocation() {
        return configuration -> {
            List<String> locations = new ArrayList<>(
                    Arrays.stream(configuration.getLocations())
                            .map(Location::getDescriptor)
                            .toList()
            );
            if (!locations.contains(LOCATION)) {
                locations.add(LOCATION);
            }
            configuration.locations(locations.toArray(String[]::new));
        };
    }
}
