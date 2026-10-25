package com.trustvault.permission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
public class DocumentAccessRequestController {

    private final DocumentAccessRequestService service;

    public DocumentAccessRequestController(DocumentAccessRequestService service) {
        this.service = service;
    }

    public record CreateRequest(
            @NotNull Long documentId,
            @NotNull DocumentPermission permission) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentAccessRequestService.RequestView request(
            Authentication authentication,
            @Valid @RequestBody CreateRequest request) {
        return service.requestAccess(
                request.documentId(),
                authentication.getName(),
                request.permission());
    }

    @GetMapping("/mine")
    public List<DocumentAccessRequestService.RequestView> mine(
            Authentication authentication) {
        return service.myRequests(authentication.getName());
    }

    @GetMapping("/pending")
    public List<DocumentAccessRequestService.RequestView> pending(
            Authentication authentication) {
        return service.pendingForOwner(authentication.getName());
    }

    @PostMapping("/{requestId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(
            Authentication authentication,
            @PathVariable Long requestId) {
        service.resolve(requestId, authentication.getName(), true);
    }

    @PostMapping("/{requestId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(
            Authentication authentication,
            @PathVariable Long requestId) {
        service.resolve(requestId, authentication.getName(), false);
    }
}
