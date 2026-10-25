package com.trustvault.permission;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentAccessRequestRepository
        extends JpaRepository<DocumentAccessRequest, Long> {

    List<DocumentAccessRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    List<DocumentAccessRequest> findByDocumentOwnerIdAndStatusOrderByCreatedAtDesc(
            Long ownerId, AccessRequestStatus status);

    Optional<DocumentAccessRequest> findByIdAndDocumentOwnerId(Long id, Long ownerId);

    boolean existsByDocumentIdAndRequesterIdAndStatus(
            Long documentId, Long requesterId, AccessRequestStatus status);
}
