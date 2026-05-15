package io.lpsoft.core.evento;

import com.fasterxml.jackson.databind.JsonNode;
import io.lpsoft.core.shared.BaseIntegrationTest;
import io.lpsoft.core.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(EventoCriadoCapturador.class)
class EventoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    EventoRepository eventos;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    EventoCriadoCapturador capturador;

    String token;

    @BeforeEach
    void preparar() throws Exception {
        eventos.deleteAll();
        usuarios.deleteAll();
        capturador.limpar();
        token = registrarELogar("maria@example.com", "senha12345");
    }

    @Test
    void deve_criar_evento_autenticado_e_publicar_EventoCriado() throws Exception {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);

        String body = """
                {"titulo":"Reunião de planejamento","descricao":"alinhar Q3",
                "inicio":"%s","fim":"%s"}
                """.formatted(inicio.toString(), fim.toString());

        mvc.perform(post("/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Reunião de planejamento"))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        assertThat(eventos.count()).isEqualTo(1);
        assertThat(capturador.recebidos()).hasSize(1);
        assertThat(capturador.recebidos().get(0).titulo()).isEqualTo("Reunião de planejamento");
        assertThat(capturador.recebidos().get(0).inicio()).isEqualTo(inicio);
    }

    @Test
    void deve_listar_apenas_eventos_do_proprio_usuario() throws Exception {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant fim = inicio.plus(30, ChronoUnit.MINUTES);

        criarEvento(token, "Meu evento", inicio, fim);

        String tokenOutro = registrarELogar("outro@example.com", "senha12345");
        criarEvento(tokenOutro, "Evento alheio", inicio, fim);

        mvc.perform(get("/eventos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Meu evento"));
    }

    @Test
    void deve_rejeitar_criacao_sem_autenticacao() throws Exception {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        String body = """
                {"titulo":"x","inicio":"%s","fim":"%s"}
                """.formatted(inicio, inicio.plus(1, ChronoUnit.HOURS));

        mvc.perform(post("/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_rejeitar_evento_com_fim_anterior_ao_inicio() throws Exception {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant fim = inicio.minus(30, ChronoUnit.MINUTES);

        String body = """
                {"titulo":"Inválido","inicio":"%s","fim":"%s"}
                """.formatted(inicio, fim);

        mvc.perform(post("/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        assertThat(eventos.count()).isZero();
        assertThat(capturador.recebidos()).isEmpty();
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String registrarELogar(String email, String senha) throws Exception {
        mvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","email":"%s","senha":"%s"}
                                """.formatted(email, email, senha)))
                .andExpect(status().isCreated());

        String resposta = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = om.readTree(resposta);
        return json.get("token").asText();
    }

    private void criarEvento(String token, String titulo, Instant inicio, Instant fim) throws Exception {
        mvc.perform(post("/eventos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"%s","inicio":"%s","fim":"%s"}
                                """.formatted(titulo, inicio, fim)))
                .andExpect(status().isCreated());
    }
}
