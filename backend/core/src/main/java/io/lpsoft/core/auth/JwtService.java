package io.lpsoft.core.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties props;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.algorithm = Algorithm.HMAC256(props.secret());
        this.verifier = JWT.require(algorithm).withIssuer(props.issuer()).build();
    }

    public String emitir(UUID usuarioId, String email) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(Duration.ofHours(props.expirationHours()));
        return JWT.create()
                .withIssuer(props.issuer())
                .withSubject(usuarioId.toString())
                .withClaim("email", email)
                .withIssuedAt(agora)
                .withExpiresAt(expira)
                .sign(algorithm);
    }

    public Optional<UUID> validar(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.of(UUID.fromString(decoded.getSubject()));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
