package br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.repository;

import br.com.jmcodestudio.autenticacaotemplate.adapters.persistence.jpa.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A JPA repository interface for managing {@link AuditLogEntity} entities.
 * This interface provides data access methods for performing CRUD operations
 * and querying the audit log database table.
 *
 * It extends {@link JpaRepository}, which offers a variety of out-of-the-box
 * persistence methods, such as saving, deleting, and finding entities by their ID.
 *
 * Purpose:
 * Serves as an adapter in the persistence layer, facilitating the integration
 * between the application's domain logic and the database. This repository leverages
 * Spring Data JPA to handle interactions with the "audit_log" table in a simplified manner.
 */
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {}