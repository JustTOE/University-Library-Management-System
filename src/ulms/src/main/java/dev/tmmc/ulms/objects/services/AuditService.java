package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.AuditLog;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.repositories.AuditLogRepository;
import dev.tmmc.ulms.security.JwtPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, String detail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer actorId = null;
        String actorEmail = null;
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal principal) {
            actorId = principal.userId();
            actorEmail = principal.email();
        }
        record(action, actorId, actorEmail, detail);
    }
}
