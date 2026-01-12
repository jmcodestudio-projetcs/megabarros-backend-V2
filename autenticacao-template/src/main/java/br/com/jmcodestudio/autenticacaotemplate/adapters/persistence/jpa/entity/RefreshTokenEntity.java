package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a refresh token entity mapped to the "refresh_token" table in the database.
 * This class defines the structure for storing data related to refresh tokens and their lifecycle.
 *
 * Annotations are used to map the class attributes to the corresponding database columns.
 *
 * Key fields include:
 * - id: The unique identifier for the refresh token.
 * - userId: The identifier of the user to whom the token belongs.
 * - tokenHash: The hash of the refresh token, used for secure storage and validation.
 * - jti: The unique identifier of the token, compliant with JWT standards.
 * - issuedAt: The timestamp when the token was issued.
 * - expiresAt: The timestamp indicating when the token will expire.
 * - revokedAt: The timestamp indicating when the token was revoked, if applicable.
 * - replacedBy: A reference to another token that replaced this token, if applicable.
 * - reason: The reason for the token's invalidation or replacement, if applicable.
 *
 * This entity is part of the persistence layer and uses JPA annotations
 * to enable ORM (Object-Relational Mapping) for database interactions.
 *
 * O que fazem: mapeiam o schema do Postgres para entidades JPA.
 * Por que: adapters de persistência convertem entre entidade JPA e records dos ports.
 */

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshTokenEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by")
    private String replacedBy;

    @Column(name = "reason")
    private String reason;

}