package com.trustvault.permission;

import com.trustvault.audit.AuditService;
import com.trustvault.document.Document;
import com.trustvault.document.DocumentRepository;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentAccessRequestService {

    private final DocumentAccessRequestRepository requestRepository;
    private final DocumentRepository documentRepository;
    private final AppUserRepository userRepository;
    private final DocumentGrantRepository grantRepository;
    private final AuditService auditService;

    public DocumentAccessRequestService(
            DocumentAccessRequestRepository requestRepository,
            DocumentRepository documentRepository,
            AppUserRepository userRepository,
            DocumentGrantRepository grantRepository,
            AuditService auditService) {
        this.requestRepository = requestRepository;
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.grantRepository = grantRepository;
        this.auditService = auditService;
    }

    public record RequestView(
            Long id, Long documentId, String documentName,
            String requesterEmail, String permission,
            String status, String createdAt) {}

    @Transactional
    public RequestView requestAccess(
            Long documentId, String requesterEmail,
            DocumentPermission permission) {

        AppUser requester = findUser(requesterEmail);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found."));

        if (document.getOwner().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You already own this document.");
        }

        if (grantRepository.findByDocumentIdAndUserId(
                documentId, requester.getId()).isPresent()) {
            throw new IllegalArgumentException("You already have access to this document.");
        }

        if (requestRepository.existsByDocumentIdAndRequesterIdAndStatus(
                documentId, requester.getId(), AccessRequestStatus.PENDING)) {
            throw new IllegalArgumentException("You already have a pending request.");
        }

        DocumentAccessRequest request =
                requestRepository.save(new DocumentAccessRequest(
                        document, requester, permission));

        auditService.record(requesterEmail, "ACCESS_REQUESTED",
                documentId, "SUCCESS", "Requested " + permission + " access");

        return view(request);
    }

    @Transactional(readOnly = true)
    public List<RequestView> myRequests(String email) {
        AppUser user = findUser(email);
        return requestRepository
                .findByRequesterIdOrderByCreatedAtDesc(user.getId())
                .stream().map(DocumentAccessRequestService::view).toList();
    }

    @Transactional(readOnly = true)
    public List<RequestView> pendingForOwner(String email) {
        AppUser owner = findUser(email);
        return requestRepository
                .findByDocumentOwnerIdAndStatusOrderByCreatedAtDesc(
                        owner.getId(), AccessRequestStatus.PENDING)
                .stream().map(DocumentAccessRequestService::view).toList();
    }

    @Transactional
    public void resolve(Long requestId, String ownerEmail, boolean approve) {
        AppUser owner = findUser(ownerEmail);

        DocumentAccessRequest request = requestRepository
                .findByIdAndDocumentOwnerId(requestId, owner.getId())
                .orElseThrow(() -> new AccessDeniedException(
                        "Request not found or you do not own this document."));

        request.resolve(approve
                ? AccessRequestStatus.APPROVED
                : AccessRequestStatus.REJECTED);

        if (approve) {
            DocumentGrant grant = grantRepository
                    .findByDocumentIdAndUserId(
                            request.getDocument().getId(),
                            request.getRequester().getId())
                    .orElse(null);

            if (grant == null) {
                grantRepository.save(new DocumentGrant(
                        request.getDocument(),
                        request.getRequester(),
                        request.getRequestedPermission()));
            } else {
                grant.setPermission(request.getRequestedPermission());
            }
        }

        auditService.record(ownerEmail,
                approve ? "ACCESS_REQUEST_APPROVED" : "ACCESS_REQUEST_REJECTED",
                request.getDocument().getId(), "SUCCESS",
                "Request " + requestId + " " + (approve ? "approved" : "rejected"));
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private static RequestView view(DocumentAccessRequest request) {
        return new RequestView(
                request.getId(),
                request.getDocument().getId(),
                request.getDocument().getName(),
                request.getRequester().getEmail(),
                request.getRequestedPermission().name(),
                request.getStatus().name(),
                request.getCreatedAt().toString());
    }
}
