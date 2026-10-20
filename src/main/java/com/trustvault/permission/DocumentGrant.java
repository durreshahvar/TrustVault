package com.trustvault.permission;

import com.trustvault.document.Document;
import com.trustvault.user.AppUser;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
    name = "document_grants",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_document_grant_user_document",
        columnNames = {"document_id", "user_id"}
    )
)
public class DocumentGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentPermission permission;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected DocumentGrant() {
        // Required by JPA
    }

    public DocumentGrant(
            Document document,
            AppUser user,
            DocumentPermission permission) {
        this.document = Objects.requireNonNull(document);
        this.user = Objects.requireNonNull(user);
        this.permission = Objects.requireNonNull(permission);
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public AppUser getUser() {
        return user;
    }

    public DocumentPermission getPermission() {
        return permission;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setPermission(DocumentPermission permission) {
        this.permission = Objects.requireNonNull(permission);
    }
}
