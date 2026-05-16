package io.lpsoft.features.analytics;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Anexa a location de migration desta feature às do core. O bean só existe
 * quando o módulo está no classpath — sem o módulo no build, a tabela de
 * agregação nunca é criada.
 */
@Configuration
public class AnalyticsFlywayConfig {

    private static final String LOCATION = "classpath:db/migration/features/analytics";

    @Bean
    public FlywayConfigurationCustomizer analyticsFlywayLocation() {
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
