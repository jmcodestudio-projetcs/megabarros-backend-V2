package br.com.jmcodestudio.autenticacaotemplate.adapters.security.jwt;

import br.com.jmcodestudio.autenticacaotemplate.application.usecase.exception.TokenInvalidException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    @Test
    void parseAccess_enforcesIssuerAndAudience() {
        String issuer = "iss";
        String audience = "aud";
        String secret = "this-is-a-very-long-secret-for-hs256-use-case-1234567890";
        long accessExpSeconds = 900;
        long refreshExpSeconds = 1209600;

        var svc = new JwtTokenService(issuer, audience, secret, accessExpSeconds, refreshExpSeconds);

        // token com audience diferente deve falhar
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        var badToken = Jwts.builder()
                .setIssuer(issuer)
                .setAudience("other-aud")
                .setSubject("1")
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setIssuedAt(Date.from(Instant.now()))
                .addClaims(java.util.Map.of("email","admin@example.com","role","ADMIN","typ","access"))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(TokenInvalidException.class, () -> svc.parseAndValidateAccess(badToken));

        // token válido gerado pelo próprio serviço deve passar
        String good = svc.generateAccessToken(1L,"admin@example.com","ADMIN", java.util.Map.of(), Instant.now());
        var claims = svc.parseAndValidateAccess(good);
        assertEquals(1L, claims.userId());
        assertEquals("admin@example.com", claims.email());
        assertEquals("ADMIN", claims.role());
    }

    @Test
    void parseRefresh_enforcesIssuerAndAudience() {
        String issuer = "iss";
        String audience = "aud";
        String secret = "this-is-a-very-long-secret-for-hs256-use-case-1234567890";
        long accessExpSeconds = 900;
        long refreshExpSeconds = 1209600;

        var svc = new JwtTokenService(issuer, audience, secret, accessExpSeconds, refreshExpSeconds);

        // refresh inválido (audience incorreta)
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        var badRefresh = Jwts.builder()
                .setIssuer(issuer)
                .setAudience("other-aud")
                .setSubject("1")
                .setExpiration(Date.from(Instant.now().plusSeconds(600)))
                .setIssuedAt(Date.from(Instant.now()))
                .setId(java.util.UUID.randomUUID().toString())
                .addClaims(java.util.Map.of("typ","refresh"))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        assertThrows(TokenInvalidException.class, () -> svc.parseAndValidateRefresh(badRefresh));

        // refresh válido
        var gen = svc.generateRefreshToken(1L, Instant.now());
        var claims = svc.parseAndValidateRefresh(gen.token());
        assertEquals(1L, claims.userId());
        assertEquals(gen.jti(), claims.jti());
    }
}