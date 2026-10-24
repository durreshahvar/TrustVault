
package com.trustvault.document;

import com.trustvault.storage.LocalFileStorageService;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final AppUserRepository userRepository;
    private final LocalFileStorageService fileStorageService;

    public DocumentService(
            DocumentRepository documentRepository,
            AppUserRepository userRepository,
            LocalFileStorageService fileStorageService) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
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
        validateMetadata(name, category, sizeBytes, contentType);

        Document document = new Document(
                name.trim(),
                java.util.UUID.randomUUID().toString(),
                category.trim(),
                sizeBytes,
                contentType.trim(),
                owner);

        return documentRepository.save(document);
    }

    @Transactional
    public Document uploadDocument(
            String authenticatedEmail,
            String name,
            String category,
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A non-empty file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Maximum file size is 10 MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()
                || contentType.length() > 150) {
            throw new IllegalArgumentException("Invalid content type");
        }

        AppUser owner = findUser(authenticatedEmail);
        validateMetadata(name, category, file.getSize(), contentType);

        String storageKey = fileStorageService.store(file.getInputStream());

        try {
            Document document = new Document(
                    name.trim(),
                    storageKey,
                    category.trim(),
                    file.getSize(),
                    contentType,
                    owner);

            return documentRepository.save(document);
        } catch (RuntimeException ex) {
            java.nio.file.Files.deleteIfExists(
                    fileStorageService.resolve(storageKey));
            throw ex;
        }
    }

    private void validateMetadata(
            String name, String category, long sizeBytes, String contentType) {

        if (name == null || name.isBlank() || name.length() > 255) {
            throw new IllegalArgumentException("Invalid document name");
        }
        if (category == null || category.isBlank() || category.length() > 100) {
            throw new IllegalArgumentException("Invalid category");
        }
        if (contentType == null || contentType.isBlank()
                || contentType.length() > 150) {
            throw new IllegalArgumentException("Invalid content type");
        }
        if (sizeBytes < 0 || sizeBytes > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Invalid file size");
        }
    }

    private AppUser findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new AccessDeniedException("Access denied"));
    }
}

