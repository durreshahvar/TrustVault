package com.trustvault.document;

import com.trustvault.permission.DocumentGrantRepository;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentDiscoveryController {

    private final DocumentRepository documents;
    private final AppUserRepository users;
    private final DocumentGrantRepository grants;

    public DocumentDiscoveryController(
            DocumentRepository documents,
            AppUserRepository users,
            DocumentGrantRepository grants) {
        this.documents = documents;
        this.users = users;
        this.grants = grants;
    }

    public record DiscoveredDocument(
            Long id,
            String name,
            String category,
            long sizeBytes,
            String contentType,
            String ownerEmail,
            Instant createdAt,
            boolean ownedByMe,
            boolean canAccess) {}

    @GetMapping("/discover")
    @Transactional(readOnly = true)
    public List<DiscoveredDocument> discover(Authentication authentication) {
        AppUser current = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Signed-in user not found"));

        return documents.findAll().stream()
                .map(doc -> {
                    boolean owner = doc.getOwner().getId().equals(current.getId());
                    boolean granted = owner || grants
                            .findByDocumentIdAndUserId(doc.getId(), current.getId())
                            .isPresent();

                    return new DiscoveredDocument(
                            doc.getId(),
                            doc.getName(),
                            doc.getCategory(),
                            doc.getSizeBytes(),
                            doc.getContentType(),
                            doc.getOwner().getEmail(),
                            doc.getCreatedAt(),
                            owner,
                            granted);
                })
                .toList();
    }
}
