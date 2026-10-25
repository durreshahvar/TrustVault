package com.trustvault.document;

import com.trustvault.audit.AuditService;
import com.trustvault.permission.DocumentGrant;
import com.trustvault.permission.DocumentGrantRepository;
import com.trustvault.permission.DocumentPermission;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentAccessService {

    private final DocumentRepository documentRepository;
    private final AppUserRepository userRepository;
    private final DocumentGrantRepository grantRepository;
    private final AuditService auditService;

    public DocumentAccessService(
            DocumentRepository documentRepository,
            AppUserRepository userRepository,
            DocumentGrantRepository grantRepository,
            AuditService auditService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.grantRepository = grantRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Document getOwnedDocument(
            Long documentId, String authenticatedEmail) {
        try {
            AppUser user = findUser(authenticatedEmail);

            Document document = documentRepository
                    .findByIdAndOwnerId(documentId, user.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            record(authenticatedEmail, "OWNERSHIP_CHECK",
                    documentId, "SUCCESS", "Owner access verified");
            return document;
        } catch (AccessDeniedException ex) {
            record(authenticatedEmail, "OWNERSHIP_CHECK",
                    documentId, "DENIED", "Ownership check failed");
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Document getReadableDocument(
            Long documentId, String authenticatedEmail) {
        try {
            AppUser user = findUser(authenticatedEmail);
            Document document = findDocument(documentId);

            if (document.getOwner().getId().equals(user.getId())) {
                record(authenticatedEmail, "READ_ACCESS",
                        documentId, "SUCCESS", "Owner access");
                return document;
            }

            grantRepository
                    .findByDocumentIdAndUserId(documentId, user.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            record(authenticatedEmail, "READ_ACCESS",
                    documentId, "SUCCESS", "Granted access");
            return document;
        } catch (AccessDeniedException ex) {
            record(authenticatedEmail, "READ_ACCESS",
                    documentId, "DENIED", "Read authorization failed");
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Document getDownloadableDocument(
            Long documentId, String authenticatedEmail) {
        Document document = getReadableDocument(documentId, authenticatedEmail);
        AppUser user = findUser(authenticatedEmail);

        if (!document.getOwner().getId().equals(user.getId())) {
            DocumentGrant grant = grantRepository
                    .findByDocumentIdAndUserId(documentId, user.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            if (grant.getPermission() != DocumentPermission.EDIT) {
                record(authenticatedEmail, "DOCUMENT_DOWNLOAD",
                        documentId, "DENIED", "VIEW ONLY permission");
                throw new AccessDeniedException(
                        "This document is view-only. Download is not permitted.");
            }
        }

        record(authenticatedEmail, "DOCUMENT_DOWNLOAD",
                documentId, "SUCCESS", "Download permission verified");
        return document;
    }

    @Transactional(readOnly = true)
    public Document getEditableDocument(
            Long documentId, String authenticatedEmail) {
        try {
            AppUser user = findUser(authenticatedEmail);
            Document document = findDocument(documentId);

            if (document.getOwner().getId().equals(user.getId())) {
                record(authenticatedEmail, "EDIT_ACCESS",
                        documentId, "SUCCESS", "Owner access");
                return document;
            }

            DocumentGrant grant = grantRepository
                    .findByDocumentIdAndUserId(documentId, user.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            if (grant.getPermission() != DocumentPermission.EDIT) {
                throw new AccessDeniedException("Access denied");
            }

            record(authenticatedEmail, "EDIT_ACCESS",
                    documentId, "SUCCESS", "EDIT permission verified");
            return document;
        } catch (AccessDeniedException ex) {
            record(authenticatedEmail, "EDIT_ACCESS",
                    documentId, "DENIED", "Edit authorization failed");
            throw ex;
        }
    }

    @Transactional
    public void grantAccess(
            Long documentId,
            String ownerEmail,
            String targetEmail,
            DocumentPermission permission) {
        try {
            AppUser owner = findUser(ownerEmail);
            Document document = documentRepository
                    .findByIdAndOwnerId(documentId, owner.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            AppUser target = findUser(targetEmail);

            if (target.getId().equals(owner.getId())) {
                throw new IllegalArgumentException(
                        "The owner already has full access");
            }

            DocumentGrant grant = grantRepository
                    .findByDocumentIdAndUserId(documentId, target.getId())
                    .orElse(null);

            if (grant == null) {
                grantRepository.save(
                        new DocumentGrant(document, target, permission));
            } else {
                grant.setPermission(permission);
            }

            record(ownerEmail, "DOCUMENT_SHARED",
                    documentId, "SUCCESS", "Permission granted or updated");
        } catch (AccessDeniedException ex) {
            record(ownerEmail, "DOCUMENT_SHARED",
                    documentId, "DENIED", "Sharing authorization failed");
            throw ex;
        }
    }

    @Transactional
    public void revokeAccess(
            Long documentId,
            String ownerEmail,
            String targetEmail) {
        try {
            AppUser owner = findUser(ownerEmail);
            documentRepository
                    .findByIdAndOwnerId(documentId, owner.getId())
                    .orElseThrow(() ->
                            new AccessDeniedException("Access denied"));

            AppUser target = findUser(targetEmail);
            grantRepository.deleteByDocumentIdAndUserId(
                    documentId, target.getId());

            record(ownerEmail, "ACCESS_REVOKED",
                    documentId, "SUCCESS", "Access revocation requested");
        } catch (AccessDeniedException ex) {
            record(ownerEmail, "ACCESS_REVOKED",
                    documentId, "DENIED", "Revocation authorization failed");
            throw ex;
        }
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));
    }

    private Document findDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));
    }

    private void record(
            String actor,
            String action,
            Long documentId,
            String outcome,
            String details) {
        auditService.record(actor, action, documentId, outcome, details);
    }
}