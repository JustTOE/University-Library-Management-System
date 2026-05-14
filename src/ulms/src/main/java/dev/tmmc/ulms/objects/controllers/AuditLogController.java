package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.AuditLogResponse;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.repositories.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-log")
@Tag(name = "Audit Log", description = "Read-only audit trail of security-relevant events (admin only)")
public class AuditLogController {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List audit-log entries newest first; filter by action and/or actor id")
    public Page<AuditLogResponse> list(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Integer actorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        int safePage = Math.max(0, page);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return repository.findFiltered(action, actorId, pageable)
                .map(AuditLogResponse::from);
    }
}
