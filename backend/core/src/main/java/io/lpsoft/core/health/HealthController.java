package io.lpsoft.core.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
            "status", "up",
            "service", "lpsoft-core",
            "timestamp", Instant.now().toString()
        );
    }
}
