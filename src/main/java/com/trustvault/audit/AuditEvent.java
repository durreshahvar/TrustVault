package com.trustvault.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_actor", columnList = "actor_email"),
    @Index(name = "idx_audit_time", columnList = "occurred_at"),
    @Index(name = "idx_audit_document", columnList = "document_id")
})
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_email", nullable = false, length = 254)
    private String actorEmail;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "document_id")
    private Long documentId;

    @Column(nullable = false, length = 20)
    private String outcome;

    @Column(length = 500)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {
    }

    public AuditEvent(
            String actorEmail,
            String action,
            Long documentId,
            String outcome,
            String details) {
        this.actorEmail = actorEmail;
        this.action = action;
        this.documentId = documentId;
        this.outcome = outcome;
        this.details = details;
        this.occurredAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public String getAction() {
        return action;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getOutcome() {
        return outcome;
    }

    public String getDetails() {
        return details;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
