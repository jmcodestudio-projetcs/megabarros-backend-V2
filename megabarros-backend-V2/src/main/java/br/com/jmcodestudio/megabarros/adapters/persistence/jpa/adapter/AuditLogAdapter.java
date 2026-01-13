package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.adapter;

import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity.AuditLogEntity;
import br.com.jmcodestudio.megabarros.adapters.persistence.jpa.repository.AuditLogJpaRepository;
import br.com.jmcodestudio.megabarros.application.port.out.AuditLogPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * An adapter implementation of the {@link AuditLogPort} interface that provides
 * functionality for recording audit log entries into the persistence database.
 *
 * This class serves as the bridge between the domain layer and the persistence
 * layer, utilizing the {@link AuditLogJpaRepository} for data storage and retrieval.
 * It converts domain objects into entities suitable for persistence and handles
 * serialization of metadata.
 *
 * Dependencies:
 * - {@link AuditLogJpaRepository}: The JPA repository used for saving audit log entries.
 * - {@link ObjectMapper}: A JSON utility for serializing and deserializing objects.
 *
 * Responsibilities:
 * - Maps the {@link Entry} objects from the domain layer to
 *   {@link AuditLogEntity} objects for database storage.
 * - Provides robust handling for metadata serialization, ensuring that failure in
 *   serialization does not impact the creation of the audit log entry, by defaulting
 *   metadata to null in case of exceptions.
 * - Uses the JPA repository to persist audit log entries in the database.
 *
 * Implementa AuditLogPort usando JPA; mapeia para a tabela audit_log.
 */
@Component
public class AuditLogAdapter implements AuditLogPort {

    private final AuditLogJpaRepository repo;
    private final ObjectMapper json;

    public AuditLogAdapter(AuditLogJpaRepository repo, ObjectMapper json) {
        this.repo = repo;
        this.json = json;
    }

    @Override
    public void record(Entry e) {
        var entity = new AuditLogEntity();
        entity.setOccurredAt(e.occurredAt());
        entity.setUserId(e.userId());
        entity.setAction(e.action());
        entity.setSubject(e.subject());
        entity.setIp(e.ip());
        entity.setUserAgent(e.userAgent());
        try {
            entity.setMetadata(e.metadata() == null ? null : json.writeValueAsString(e.metadata()));
        } catch (Exception ex) {
            entity.setMetadata(null);
        }
        repo.save(entity);
    }
}