package io.lpsoft.core.auth;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit puro (sem Spring) do emissor/validador JWT — segurança, baixo custo.
 */
class JwtServiceTest {

    private static final String SECRET =
            "test-secret-must-be-at-least-256-bits-long-for-hmac256-algo";
    private static final String ISSUER = "lpsoft-test";

    private JwtService service(int expirationHours, String issuer) {
        return new JwtService(new JwtProperties(SECRET, expirationHours, issuer));
    }

    @Test
    void emitir_e_validar_round_trip() {
        JwtService svc = service(1, ISSUER);
        UUID id = UUID.randomUUID();

        String token = svc.emitir(id, "user@example.com");

        assertThat(svc.validar(token)).contains(id);
    }

    @Test
    void token_de_outro_issuer_e_rejeitado() {
        UUID id = UUID.randomUUID();
        String tokenOutro = service(1, "outro-issuer").emitir(id, "user@example.com");

        assertThat(service(1, ISSUER).validar(tokenOutro)).isEmpty();
    }

    @Test
    void token_corrompido_retorna_empty() {
        assertThat(service(1, ISSUER).validar("abc.def.ghi")).isEmpty();
    }

    @Test
    void token_expirado_retorna_empty() {
        UUID id = UUID.randomUUID();
        // expirationHours negativo → expira no passado já na emissão.
        String expirado = service(-1, ISSUER).emitir(id, "user@example.com");

        assertThat(service(1, ISSUER).validar(expirado)).isEmpty();
    }
}
