package com.bcndeigth.carebridgeapi.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bcndeigth.carebridgeapi.users.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class TokenService {

    private static final String ISSUER = "carebridge-api";

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("role", usuario.getTipoPerfil().name())
                    .withExpiresAt(Date.from(Instant.now().plus(2, ChronoUnit.HOURS)))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Não foi possível gerar o token de autenticação.", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);

            return decodedJWT.getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }
}