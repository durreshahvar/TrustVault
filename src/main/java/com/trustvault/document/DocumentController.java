
package com.trustvault.document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    public record CreateDocumentRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 100) String category,
            @PositiveOrZero long sizeBytes,
            @NotBlank @Size(max = 150) String contentType
    ) {}

    public record DocumentResponse(
            Long id,
            String name,
            String category,
            long sizeBytes,
            String contentType,
            String ownerEmail,
            Instant createdAt
    ) {
        static DocumentResponse from(Document document) {
            return new DocumentResponse(
                    document.getId(),
                    document.getName(),
                    document.getCategory(),
                    document.getSizeBytes(),
                    document.getContentType(),
                    document.getOwner().getEmail(),
                    document.getCreatedAt()
            );
        }
    }

    @GetMapping
    public List<DocumentResponse> listMyDocuments(
            Authentication authentication) {
        return documentService.listMyDocuments(authentication.getName())
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse createDocumentRecord(
            Authentication authentication,
            @Valid @RequestBody CreateDocumentRequest request) {
        Document document = documentService.createDocumentRecord(
                authentication.getName(),
                request.name(),
                request.category(),
                request.sizeBytes(),
                request.contentType()
        );
        return DocumentResponse.from(document);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse uploadDocument(
            Authentication authentication,
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file) throws IOException {

        Document document = documentService.uploadDocument(
                authentication.getName(), name, category, file);
        return DocumentResponse.from(document);
    }
}

