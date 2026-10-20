package com.trustvault.permission;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentGrantRepository
        extends JpaRepository<DocumentGrant, Long> {

    Optional<DocumentGrant> findByDocumentIdAndUserId(
            Long documentId, Long userId);

    void deleteByDocumentIdAndUserId(
            Long documentId, Long userId);
}
