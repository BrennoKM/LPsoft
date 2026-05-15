package io.lpsoft.features.categorias;

import org.flywaydb.core.api.Location;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mecanismo central da LPS para migrations: este bean só existe quando o
 * módulo da feature está no classpath. Ele ANEXA a location da feature às
 * locations já configuradas (core), sem substituí-las.
 *
 * Sem o módulo no build → bean não existe → location não é adicionada →
 * migration de categoria nunca roda. Com o módulo → tabela é criada.
 */
@Configuration
public class CategoriasFlywayConfig {

    private static final String LOCATION = "classpath:db/migration/features/categorias";

    @Bean
    public FlywayConfigurationCustomizer categoriasFlywayLocation() {
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
