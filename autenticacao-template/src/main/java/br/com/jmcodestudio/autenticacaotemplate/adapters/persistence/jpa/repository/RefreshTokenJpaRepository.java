package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.repository;

import br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * A JPA repository interface for managing {@link RefreshTokenEntity} entities.
 * This interface provides data access methods for executing CRUD operations
 * and custom queries on the refresh token database table.
 *
 * It extends {@link JpaRepository}, offering a wide range of standard persistence operations.
 *
 * Key methods include:
 * - {@code findByTokenHash(String tokenHash)}: Finds a refresh token entity by its hashed token value.
 * - {@code findByUserId(Long userId)}: Retrieves a list of refresh token entities associated with a given user ID.
 *
 * Purpose:
 * Integrates the persistence layer with the application's domain model, acting as an adapter
 * that bridges the gap between the JPA-managed entities and the business logic.
 *
 * This repository leverages Spring Data JPA features to simplify database interactions.
 *
 * Operações CRUD diretas nas entidades
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    List<RefreshTokenEntity> findByUserId(Long userId);
}