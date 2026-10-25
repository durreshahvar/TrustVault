
package com.trustvault.document;

import com.trustvault.audit.AuditService;
import com.trustvault.permission.DocumentPermission;
import com.trustvault.storage.LocalFileStorageService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAccessService documentAccessService;
    private final LocalFileStorageService fileStorageService;
    private final AuditService auditService;

    public DocumentController(
            DocumentService documentService,
            DocumentAccessService documentAccessService,
            LocalFileStorageService fileStorageService,
            AuditService auditService) {

        this.documentService = documentService;
        this.documentAccessService = documentAccessService;
        this.fileStorageService = fileStorageService;
        this.auditService = auditService;
    }

    public record CreateDocumentRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 100) String category,
            @PositiveOrZero long sizeBytes,
            @NotBlank @Size(max = 150) String contentType
    ) {}

    public record ShareDocumentRequest(
            @NotBlank @Email @Size(max = 254) String targetEmail,
            @NotNull DocumentPermission permission
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

        return documentService
                .listMyDocuments(authentication.getName())
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    // Create a metadata-only record.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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

        auditService.record(
                authentication.getName(),
                "DOCUMENT_CREATED",
                document.getId(),
                "SUCCESS",
                "Document metadata created"
        );

        return DocumentResponse.from(document);
    }

    // Upload a real file.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse uploadDocument(
            Authentication authentication,
            @RequestParam("name") String name,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file)
            throws IOException {

        Document document = documentService.uploadDocument(
                authentication.getName(),
                name,
                category,
                file
        );

        auditService.record(
                authentication.getName(),
                "DOCUMENT_UPLOADED",
                document.getId(),
                "SUCCESS",
                "Document uploaded"
        );

        return DocumentResponse.from(document);
    }

    // View supported document types inline when the user has access.
    @GetMapping("/{documentId}/view")
    public ResponseEntity<Resource> viewDocument(
            Authentication authentication,
            @PathVariable Long documentId) throws IOException {

        Document document = documentAccessService.getReadableDocument(
                documentId, authentication.getName());
        Path path = fileStorageService.resolve(document.getStorageKey());

        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Document file not found");
        }

        String name = document.getName() == null
                ? "" : document.getName().toLowerCase(java.util.Locale.ROOT);

        MediaType type = MediaType.APPLICATION_OCTET_STREAM;
        byte[] sig = new byte[5];
        int count;
        try (var input = Files.newInputStream(path)) {
            count = input.read(sig);
        }

        if (count >= 5 && sig[0] == '%' && sig[1] == 'P'
                && sig[2] == 'D' && sig[3] == 'F' && sig[4] == '-') {
            type = MediaType.APPLICATION_PDF;
        } else if (name.endsWith(".pdf")) {
            type = MediaType.APPLICATION_PDF;
        } else if (name.endsWith(".png")) {
            type = MediaType.IMAGE_PNG;
        } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            type = MediaType.IMAGE_JPEG;
        } else if (name.endsWith(".gif")) {
            type = MediaType.IMAGE_GIF;
        } else if (name.endsWith(".txt")) {
            type = MediaType.TEXT_PLAIN;
        } else {
            try {
                String stored = document.getContentType();
                if (stored != null) {
                    MediaType candidate = MediaType.parseMediaType(stored);
                    if (candidate.equals(MediaType.APPLICATION_PDF)
                            || candidate.getType().equals("image")
                            || candidate.equals(MediaType.TEXT_PLAIN)) {
                        type = candidate;
                    }
                }
            } catch (org.springframework.http.InvalidMediaTypeException ignored) {
                // Keep the safe default for unknown content types.
            }
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(document.getName()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentLength(Files.size(path))
                .body(new FileSystemResource(path));
    }


    // Convert Office documents to a private, browser-viewable PDF.
    @GetMapping("/{documentId}/preview-pdf")
    public ResponseEntity<org.springframework.core.io.Resource> previewOfficeAsPdf(
            Authentication authentication,
            @PathVariable Long documentId) throws IOException {

        Document document = documentAccessService.getReadableDocument(
                documentId, authentication.getName());
        Path source = fileStorageService.resolve(document.getStorageKey());

        if (!Files.isRegularFile(source)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found");
        }

        String originalName = document.getName() == null ? "document" : document.getName();
        String lowerName = originalName.toLowerCase(java.util.Locale.ROOT);
        String contentType = document.getContentType() == null ? "" :
                document.getContentType().toLowerCase(java.util.Locale.ROOT);

        String extension = "";
        int dot = lowerName.lastIndexOf('.');
        if (dot >= 0) extension = lowerName.substring(dot);

        if (extension.isEmpty()) {
            if (contentType.contains("wordprocessingml")) extension = ".docx";
            else if (contentType.contains("msword")) extension = ".doc";
            else if (contentType.contains("spreadsheetml")) extension = ".xlsx";
            else if (contentType.contains("ms-excel")) extension = ".xls";
            else if (contentType.contains("presentationml")) extension = ".pptx";
            else if (contentType.contains("powerpoint")) extension = ".ppt";
            else throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Cannot identify this Office document format");
        }

        if (!java.util.Set.of(".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx")
                .contains(extension)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "This endpoint previews Word, Excel and PowerPoint files");
        }

        Path tempDir = Files.createTempDirectory("trustvault-preview-");
        try {
            Path input = tempDir.resolve("document" + extension);
            Path output = tempDir.resolve("document.pdf");
            Path log = tempDir.resolve("converter.log");
            Files.copy(source, input);

            Process process = new ProcessBuilder(
                    "libreoffice", "-env:UserInstallation=file://" +
                    tempDir.resolve("lo-profile").toAbsolutePath(),
                    "--headless", "--convert-to", "pdf", "--outdir",
                    tempDir.toString(), input.toString())
                    .redirectErrorStream(true)
                    .redirectOutput(log.toFile())
                    .start();

            if (!process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                        "Document conversion timed out");
            }
            if (process.exitValue() != 0 || !Files.isRegularFile(output)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Could not convert this document for browser preview");
            }

            byte[] pdf = Files.readAllBytes(output);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline().filename("preview.pdf").build().toString())
                    .header("X-Content-Type-Options", "nosniff")
                    .contentLength(pdf.length)
                    .body(new org.springframework.core.io.ByteArrayResource(pdf));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Document preview was interrupted");
        } finally {
            try (var paths = Files.walk(tempDir)) {
                paths.sorted(java.util.Comparator.reverseOrder())
                        .forEach(item -> {
                            try { Files.deleteIfExists(item); }
                            catch (IOException ignored) {}
                        });
            }
        }
    }

    // Download only when the user has READ or EDIT access.
    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            Authentication authentication,
            @PathVariable Long documentId) throws IOException {

        Document document = documentAccessService.getDownloadableDocument(
                documentId,
                authentication.getName()
        );

        Path path = fileStorageService.resolve(
                document.getStorageKey()
        );

        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Document file not found"
            );
        }

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(
                    document.getContentType()
            );
        } catch (org.springframework.http.InvalidMediaTypeException ex) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        auditService.record(
                authentication.getName(),
                "DOCUMENT_DOWNLOADED",
                document.getId(),
                "SUCCESS",
                "Authorized document download"
        );

        Resource resource = new FileSystemResource(path);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(document.getName())
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(path))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .body(resource);
    }

    // Grant READ or EDIT access to another registered user.
    @PostMapping("/{documentId}/grants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grantAccess(
            Authentication authentication,
            @PathVariable Long documentId,
            @Valid @RequestBody ShareDocumentRequest request) {

        documentAccessService.grantAccess(
                documentId,
                authentication.getName(),
                request.targetEmail(),
                request.permission()
        );
    }

    // Revoke a user's access.
    @DeleteMapping("/{documentId}/grants/{targetEmail}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAccess(
            Authentication authentication,
            @PathVariable Long documentId,
            @PathVariable @Email String targetEmail) {

        documentAccessService.revokeAccess(
                documentId,
                authentication.getName(),
                targetEmail
        );
    }
}