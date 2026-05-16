package io.lpsoft.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Ponto de entrada. O bootstrap vive fora de core/features para que o
 * classpath scanning encontre core e qualquer feature presente no build —
 * sem registro manual. Features ausentes simplesmente não estão no classpath.
 *
 * scanBasePackages cobre @Component/@Service/@RestController. JPA precisa de
 * varredura explícita (defaults usariam só io.lpsoft.app), por isso
 * @EnableJpaRepositories e @EntityScan apontam para io.lpsoft (core + features).
 */
@SpringBootApplication(scanBasePackages = "io.lpsoft")
@EnableJpaRepositories(basePackages = "io.lpsoft")
@EntityScan(basePackages = "io.lpsoft")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
