package br.com.jmcodestudio.megabarros.application.port.out;

import java.time.Instant;
import java.util.Map;

/**
 * Interface defining the port for audit logging operations.
 * Allows recording log entries related to user actions or events.
 * Provides a mechanism to track and audit user behavior and system changes.
 *
 * Interface usada pelo core para registrar eventos de auditoria, sem conhecer JPA ou HTTP.
 */
public interface AuditLogPort {
    void record(Entry entry);

    record Entry(
            Instant occurredAt,
            Long userId,
            String action,
            String subject,
            String ip,
            String userAgent,
            Map<String, Object> metadata
    ) {}
}