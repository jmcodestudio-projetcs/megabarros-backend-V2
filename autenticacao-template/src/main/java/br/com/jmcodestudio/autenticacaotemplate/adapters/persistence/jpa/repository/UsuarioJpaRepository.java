package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.repository;

import br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A JPA repository interface for managing {@link UsuarioEntity} entities.
 * This interface provides data access methods for performing CRUD operations
 * and custom queries on the user database table.
 *
 * It extends {@link JpaRepository}, which provides a set of standard methods
 * to simplify database interactions, such as saving, deleting, and finding entities.
 *
 * Key methods include:
 * - {@code findByEmail(String email)}: Retrieves a user entity by its email address.
 *
 * Purpose:
 * Acts as an adapter within the persistence layer, bridging the application
 * domain logic and the database. This repository leverages the capabilities
 * of Spring Data JPA to facilitate database operations for the "usuario" table.
 *
 * Operações CRUD diretas nas entidades
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {
    Optional<UsuarioEntity> findByEmail(String email);
}