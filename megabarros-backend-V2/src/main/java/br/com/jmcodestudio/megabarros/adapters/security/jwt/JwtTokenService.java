package br.com.jmcodestudio.megabarros.adapters.security.jwt;

import br.com.jmcodestudio.megabarros.application.port.out.TokenServicePort;
import br.com.jmcodestudio.megabarros.application.usecase.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Service implementation for handling JWT-based token generation and validation.
 * <p>
 * This class provides methods for creating, validating, and parsing both access
 * and refresh tokens using the JSON Web Token (JWT) standard. It implements the
 * TokenServicePort interface to define its contract within the application core.
 * <p>
 * Features of this service include:
 * - Creation of access tokens with user-specific claims and expiration.
 * - Generation of refresh tokens with unique identifiers (jti) and extended lifetimes.
 * - Validation and parsing of access and refresh tokens to extract user-related claims.
 * <p>
 * Dependencies:
 * - Uses the jjwt library for token creation and validation.
 * - Configured through application properties for issuer, audience, secret, and expiration times.
 * <p>
 * Constructor Parameters:
 * - issuer: Identifies the principal that issued the JWT.
 * - audience: Specifies the recipients that the JWT is intended for.
 * - secret: Secret key for signing the JWTs.
 * - accessExpSeconds: Expiration time for access tokens in seconds.
 * - refreshExpSeconds: Expiration time for refresh tokens in seconds.
 * <p>
 * O que faz: implementa TokenServicePort usando JJWT e HS256.
 * Por que: isola a biblioteca JWT; adiciona claim “typ” = “access”/“refresh”; valida issuer/audience (configurável se desejar reforçar).
 * Garante que o token seja emitido pelo emissor esperado e consumido pelo público correto.
 * Qualquer JwtException é convertida em TokenInvalidException (core) para que o Handler retorne 401 consistente.
 * Gera e valida tokens JWT; reforço de issuer/audience; encapsula exceções em TokenInvalidException.
 */
@Component
public class JwtTokenService implements TokenServicePort {

    private final String issuer;
    private final String audience;
    private final Key key;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtTokenService(
            @Value("${JWT_ISSUER}") String issuer,
            @Value("${JWT_AUDIENCE}") String audience,
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_ACCESS_EXP_SECONDS}") long accessExpSeconds,
            @Value("${JWT_REFRESH_EXP_SECONDS}") long refreshExpSeconds) {
        this.issuer = issuer;
        this.audience = audience;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpSeconds = accessExpSeconds;
        this.refreshExpSeconds = refreshExpSeconds;
    }

    @Override
    public String generateAccessToken(Long userId, String email, String role, Map<String, Object> extraClaims, Instant now) {
        var exp = Date.from(now.plusSeconds(accessExpSeconds));
        var builder = Jwts.builder()
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(String.valueOf(userId))
                .setExpiration(exp)
                .setIssuedAt(Date.from(now))
                .addClaims(Map.of("email", email, "role", role, "typ", "access"));
        if (extraClaims != null) builder.addClaims(extraClaims);
        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    @Override
    public GeneratedRefresh generateRefreshToken(Long userId, Instant now) {
        var jti = UUID.randomUUID().toString(); // novo JTI sempre
        var exp = now.plusSeconds(refreshExpSeconds);
        var token = Jwts.builder()
                .setIssuer(issuer)
                .setAudience(audience)
                .setSubject(String.valueOf(userId))
                .setExpiration(Date.from(exp))
                .setIssuedAt(Date.from(now))
                .setId(jti)
                .addClaims(Map.of("typ", "refresh"))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return new GeneratedRefresh(token, jti, exp);
    }

    @Override
    public Claims parseAndValidateAccess(String token) {
        try {
            var jwt = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .require("typ", "access")
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // audience (pode ser única ou lista)
            if (jwt.getAudience() == null || !jwt.getAudience().equals(audience)) {
                throw new TokenInvalidException();
            }
            return toClaims(jwt);
        } catch (JwtException e) {
            throw new TokenInvalidException();
        }
    }

    @Override
    public Claims parseAndValidateRefresh(String token) {
        try {
            var jwt = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .require("typ", "refresh")
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (jwt.getAudience() == null || !jwt.getAudience().equals(audience)) {
                throw new TokenInvalidException();
            }
            return toClaims(jwt);
        } catch (JwtException e) {
            throw new TokenInvalidException();
        }
    }

    private Claims toClaims(io.jsonwebtoken.Claims jwt) {
        var userId = Long.valueOf(jwt.getSubject());
        var email = (String) jwt.get("email");
        var role = (String) jwt.get("role");
        var jti = jwt.getId();
        var exp = jwt.getExpiration().toInstant();
        return new Claims(userId, email, role, jti, exp);
    }
}