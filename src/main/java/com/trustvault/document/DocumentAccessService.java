package com.trustvault.document;

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

    public DocumentAccessService(
            DocumentRepository documentRepository,
            AppUserRepository userRepository,
            DocumentGrantRepository grantRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.grantRepository = grantRepository;
    }

    @Transactional(readOnly = true)
    public Document getOwnedDocument(
            Long documentId, String authenticatedEmail) {
        AppUser user = findUser(authenticatedEmail);

        return documentRepository
                .findByIdAndOwnerId(documentId, user.getId())
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));
    }

    @Transactional(readOnly = true)
    public Document getReadableDocument(
            Long documentId, String authenticatedEmail) {
        AppUser user = findUser(authenticatedEmail);
        Document document = findDocument(documentId);

        if (document.getOwner().getId().equals(user.getId())) {
            return document;
        }

        DocumentGrant grant = grantRepository
                .findByDocumentIdAndUserId(documentId, user.getId())
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));

        return document;
    }

    @Transactional(readOnly = true)
    public Document getEditableDocument(
            Long documentId, String authenticatedEmail) {
        AppUser user = findUser(authenticatedEmail);
        Document document = findDocument(documentId);

        if (document.getOwner().getId().equals(user.getId())) {
            return document;
        }

        DocumentGrant grant = grantRepository
                .findByDocumentIdAndUserId(documentId, user.getId())
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));

        if (grant.getPermission() != DocumentPermission.EDIT) {
            throw new AccessDeniedException("Access denied");
        }

        return document;
    }

    @Transactional
    public void grantAccess(
            Long documentId,
            String ownerEmail,
            String targetEmail,
            DocumentPermission permission) {
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
    }

    @Transactional
    public void revokeAccess(
            Long documentId,
            String ownerEmail,
            String targetEmail) {
        AppUser owner = findUser(ownerEmail);
        documentRepository.findByIdAndOwnerId(documentId, owner.getId())
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));

        AppUser target = findUser(targetEmail);
        grantRepository.deleteByDocumentIdAndUserId(
                documentId, target.getId());
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
}
