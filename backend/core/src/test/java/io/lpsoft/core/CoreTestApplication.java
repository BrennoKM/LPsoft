package io.lpsoft.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boot application apenas para testes do módulo core (que é uma lib, sem main).
 * Escaneia somente io.lpsoft.core — testes do core não enxergam features.
 */
@SpringBootApplication(scanBasePackages = "io.lpsoft.core")
public class CoreTestApplication {
}
