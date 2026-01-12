package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.adapter;

import br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity.RefreshTokenEntity;
import br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.repository.RefreshTokenJpaRepository;
import br.com.jmcodestudio.autenticacaotemplate.application.port.out.RefreshTokenStorePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * An adapter implementing the {@link RefreshTokenStorePort} interface to provide
 * functionality for persisting, retrieving, revoking, and rotating refresh tokens
 * in a database using JPA.
 *
 * This class acts as an intermediary between the application's domain logic
 * and the persistence layer, encapsulating the details of database operations.
 * It uses the {@link RefreshTokenJpaRepository} for data access.
 *
 * Main responsibilities:
 * - Persist new refresh tokens with expiration details.
 * - Retrieve refresh tokens by their hashed value.
 * - Revoke tokens, marking them as invalid with an optional reason.
 * - Support token rotation by revoking old tokens and creating new ones.
 * - Bulk revocation of all refresh tokens for a given user ID (utility for security events such as password changes).
 *
 * Transactional boundaries are defined for methods where multiple operations are performed.
 *
 * O que fazem: convertem entre entidade JPA e record do port e implementam operações exigidas pelos casos de uso
 *
 * Salva o novo refresh com o novo JTI.
 * Revoga todos os refresh tokens do usuário em troca de senha
 */
@Component
public class RefreshTokenStoreAdapter implements RefreshTokenStorePort {

    private final RefreshTokenJpaRepository repo;

    public RefreshTokenStoreAdapter(RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void persist(Long userId, String tokenHash, String jti, Instant expiresAt) {
        var e = new RefreshTokenEntity();
        e.setUserId(userId);
        e.setTokenHash(tokenHash);
        e.setJti(jti);
        e.setIssuedAt(Instant.now());
        e.setExpiresAt(expiresAt);
        repo.save(e);
    }

    @Override
    public Optional<RefreshTokenRecord> findByHash(String tokenHash) {
        return repo.findByTokenHash(tokenHash).map(this::map);
    }

    @Override
    @Transactional
    public void revoke(String tokenHash, String reason, Instant when) {
        var e = repo.findByTokenHash(tokenHash).orElseThrow();
        e.setRevokedAt(when);
        e.setReason(reason);
        repo.save(e);
    }

    @Override
    @Transactional
    public void rotate(String oldHash, String newHash, String newJti, Instant when, Instant newExpiry) {
        var old = repo.findByTokenHash(oldHash).orElseThrow();
        old.setRevokedAt(when);
        old.setReason("rotated");
        old.setReplacedBy(newHash);
        repo.save(old);

        var e = new RefreshTokenEntity();
        e.setUserId(old.getUserId());
        e.setTokenHash(newHash);
        e.setJti(newJti);               // novo JTI
        e.setIssuedAt(when);
        e.setExpiresAt(newExpiry);
        repo.save(e);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(Long userId, String reason, Instant when) {
        var list = repo.findByUserId(userId);
        for (var e : list) {
            if (e.getRevokedAt() == null) {
                e.setRevokedAt(when);
                e.setReason(reason);
                repo.save(e);
            }
        }
    }

    private RefreshTokenRecord map(RefreshTokenEntity e) {
        return new RefreshTokenRecord(e.getId(), e.getUserId(), e.getTokenHash(), e.getJti(), e.getIssuedAt(), e.getExpiresAt(), e.getRevokedAt(), e.getReplacedBy(), e.getReason());
    }
}