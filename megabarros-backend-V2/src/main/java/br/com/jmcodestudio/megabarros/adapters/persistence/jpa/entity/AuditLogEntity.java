package br.com.jmcodestudio.megabarros.adapters.persistence.jpa.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;

/**
 * Represents an audit log entity mapped to the "audit_log" table in the database.
 * This class defines the structure for storing audit data that tracks user actions and events.
 *
 * Key fields include:
 * - id: The unique identifier for the audit log entry.
 * - occurredAt: The timestamp indicating when the action occurred, required.
 * - userId: The identifier of the user associated with the action.
 * - action: A description of the user action, required and limited to 100 characters.
 * - subject: The subject associated with the action, limited to 150 characters.
 * - ip: The IP address from where the action was performed, limited to 64 characters.
 * - userAgent: The user agent (browser or client) used to perform the action, limited to 255 characters.
 * - metadata: Additional data related to the action, stored as a JSON string.
 *
 * This entity uses JPA annotations to map the attributes to the corresponding database schema,
 * enabling ORM (Object-Relational Mapping) for persistence and querying.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_audit")
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "subject", length = 150)
    private String subject;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata; // armazenamos JSON como String

}