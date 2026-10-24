package com.trustvault.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            String actorEmail,
            String action,
            Long documentId,
            String outcome,
            String details) {

        if (actorEmail == null || actorEmail.isBlank()) {
            actorEmail = "anonymous";
        }

        repository.save(new AuditEvent(
                actorEmail,
                action,
                documentId,
                outcome,
                details
        ));
    }
}
