package br.com.jmcodestudio.megabarros.application.port.out;

import java.time.Instant;
import java.util.Optional;

/**
 * Interface representing the port for refresh token storage operations.
 * O que faz: persiste, busca, revoga e rotaciona refresh tokens.
 * Por que: permite trocar Postgres por outra store sem tocar no core.
 * Evita downcast do adapter na troca de senha.
 * Permite gravar o novo JTI na rotação.
 * Abstrai persistência de refresh tokens; implementa rotação (com novo JTI) e revogação (inclui revogar todos por usuário).
 */
public interface RefreshTokenStorePort {
    void persist(Long userId, String tokenHash, String jti, Instant expiresAt);
    Optional<RefreshTokenRecord> findByHash(String tokenHash);
    void revoke(String tokenHash, String reason, Instant when);
    // Atualizada: rotação recebe o novo JTI
    void rotate(String oldHash, String newHash, String newJti, Instant when, Instant newExpiry);

    // Nova: revogar todos tokens do usuário (troca de senha)
    void revokeAllByUserId(Long userId, String reason, Instant when);

    record RefreshTokenRecord(Long id, Long userId, String tokenHash, String jti, Instant issuedAt, Instant expiresAt, Instant revokedAt, String replacedBy, String reason) {}
}