package com.trustvault.document;

import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AppUserRepository userRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            AppUserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Document> listMyDocuments(String authenticatedEmail) {
        AppUser user = findUser(authenticatedEmail);
        return documentRepository
                .findByOwnerIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public Document createDocumentRecord(
            String authenticatedEmail,
            String name,
            String category,
            long sizeBytes,
            String contentType) {

        AppUser owner = findUser(authenticatedEmail);

        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Invalid document name");
        }
        if (category == null || category.isBlank()
                || category.length() > 100) {
            throw new IllegalArgumentException("Invalid category");
        }
        if (contentType == null || contentType.isBlank()
                || contentType.length() > 150) {
            throw new IllegalArgumentException("Invalid content type");
        }
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("Invalid file size");
        }

        // This creates metadata only. It does not upload or store file content.
        String storageKey = UUID.randomUUID().toString();

        Document document = new Document(
                name.trim(),
                storageKey,
                category.trim(),
                sizeBytes,
                contentType.trim(),
                owner);

        return documentRepository.save(document);
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));
    }
}
