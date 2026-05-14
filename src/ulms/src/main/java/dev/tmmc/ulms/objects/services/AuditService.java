package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.AuditLog;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Persist an audit row in its own transaction so a caller's rollback
     * (e.g. InvalidCredentialsException) does not wipe the audit trail.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, Integer actorId, String actorEmail, String detail) {
        AuditLog entry = new AuditLog();
        entry.setOccurredAt(OffsetDateTime.now());
        entry.setAction(action);
        entry.setActorId(actorId);
        entry.setActorEmail(actorEmail);
        entry.setDetail(detail);
        repository.save(entry);
    }
}
