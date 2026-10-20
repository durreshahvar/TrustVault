package com.trustvault.document;

import com.trustvault.user.AppUser;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String storageKey;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 150)
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Document() {
        // Required by JPA
    }

    public Document(
            String name,
            String storageKey,
            String category,
            long sizeBytes,
            String contentType,
            AppUser owner) {
        this.name = Objects.requireNonNull(name);
        this.storageKey = Objects.requireNonNull(storageKey);
        this.category = Objects.requireNonNull(category);
        this.contentType = Objects.requireNonNull(contentType);
        this.owner = Objects.requireNonNull(owner);

        if (sizeBytes < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
        this.sizeBytes = sizeBytes;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getStorageKey() { return storageKey; }
    public String getCategory() { return category; }
    public long getSizeBytes() { return sizeBytes; }
    public String getContentType() { return contentType; }
    public AppUser getOwner() { return owner; }
    public Instant getCreatedAt() { return createdAt; }
}
