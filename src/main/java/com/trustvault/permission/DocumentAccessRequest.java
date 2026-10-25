package com.trustvault.permission;

import com.trustvault.document.Document;
import com.trustvault.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "document_access_requests",
    indexes = {
        @Index(name = "idx_access_request_document", columnList = "document_id"),
        @Index(name = "idx_access_request_requester", columnList = "requester_id")
    }
)
public class DocumentAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentPermission requestedPermission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessRequestStatus status = AccessRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant resolvedAt;

    protected DocumentAccessRequest() {}

    public DocumentAccessRequest(
            Document document,
            AppUser requester,
            DocumentPermission requestedPermission) {
        this.document = document;
        this.requester = requester;
        this.requestedPermission = requestedPermission;
    }

    public Long getId() { return id; }
    public Document getDocument() { return document; }
    public AppUser getRequester() { return requester; }
    public DocumentPermission getRequestedPermission() { return requestedPermission; }
    public AccessRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void resolve(AccessRequestStatus status) {
        if (this.status != AccessRequestStatus.PENDING) {
            throw new IllegalStateException("This request has already been resolved.");
        }
        if (status == AccessRequestStatus.PENDING) {
            throw new IllegalArgumentException("A resolved status is required.");
        }
        this.status = status;
        this.resolvedAt = Instant.now();
    }
}
