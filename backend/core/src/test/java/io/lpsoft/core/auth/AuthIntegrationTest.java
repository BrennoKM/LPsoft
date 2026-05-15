package io.lpsoft.core.auth;

import io.lpsoft.core.shared.BaseIntegrationTest;
import io.lpsoft.core.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    UsuarioRepository usuarios;

    @BeforeEach
    void limparUsuarios() {
        usuarios.deleteAll();
    }

    @Test
    void deve_registrar_e_logar_com_sucesso() throws Exception {
        String registro = """
                {"nome":"Maria","email":"maria@example.com","senha":"senha12345"}
                """;

        mvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(notNullValue()));

        String login = """
                {"email":"maria@example.com","senha":"senha12345"}
                """;

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.email").value("maria@example.com"));

        assertThat(usuarios.count()).isEqualTo(1);
    }

    @Test
    void deve_rejeitar_login_com_senha_errada() throws Exception {
        mvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome":"Joao","email":"joao@example.com","senha":"senhaCerta"}
                        """))
                .andExpect(status().isCreated());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"joao@example.com","senha":"senhaErrada"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_rejeitar_login_com_email_inexistente() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"ninguem@example.com","senha":"qualquer123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deve_rejeitar_registro_com_email_duplicado() throws Exception {
        String registro = """
                {"nome":"Ana","email":"ana@example.com","senha":"senha12345"}
                """;
        mvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
                .andExpect(status().isCreated());

        mvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registro))
                .andExpect(status().isConflict());
    }

    @Test
    void deve_rejeitar_registro_com_senha_curta() throws Exception {
        mvc.perform(post("/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Curta","email":"curta@example.com","senha":"abc"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
