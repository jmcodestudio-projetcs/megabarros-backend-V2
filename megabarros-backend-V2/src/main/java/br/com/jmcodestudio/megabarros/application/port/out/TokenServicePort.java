package br.com.jmcodestudio.megabarros.application.port.out;

import java.time.Instant;
import java.util.Map;

/**
 * Interface representing the port for token-related operations.
 * Define o contrato do port para gerar tokens.
 * O que faz: gera/valida access/refresh tokens.
 * Por que: desacopla casos de uso da lib JWT.
 */
public interface TokenServicePort {
    String generateAccessToken(Long userId, String email, String role, Map<String, Object> extraClaims, Instant now);
    GeneratedRefresh generateRefreshToken(Long userId, Instant now);
    Claims parseAndValidateAccess(String token);
    Claims parseAndValidateRefresh(String token);

    record GeneratedRefresh(String token, String jti, Instant expiresAt) {}
    record Claims(Long userId, String email, String role, String jti, Instant expiresAt) {}
}