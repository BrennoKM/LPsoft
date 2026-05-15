package io.lpsoft.core.auth;

import io.lpsoft.core.auth.dto.LoginRequest;
import io.lpsoft.core.auth.dto.RegistrarRequest;
import io.lpsoft.core.auth.dto.TokenResponse;
import io.lpsoft.core.usuario.Usuario;
import io.lpsoft.core.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    @Transactional
    public UUID registrar(RegistrarRequest req) {
        if (usuarios.existsByEmailIgnoreCase(req.email())) {
            throw new EmailJaCadastradoException(req.email());
        }
        Usuario u = Usuario.builder()
                .id(UUID.randomUUID())
                .nome(req.nome())
                .email(req.email())
                .senhaHash(passwordEncoder.encode(req.senha()))
                .criadoEm(Instant.now())
                .build();
        usuarios.save(u);
        return u.getId();
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email())
                .orElseThrow(CredenciaisInvalidasException::new);
        if (!passwordEncoder.matches(req.senha(), u.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        String token = jwt.emitir(u.getId(), u.getEmail());
        return new TokenResponse(token, u.getId(), u.getEmail());
    }
}
