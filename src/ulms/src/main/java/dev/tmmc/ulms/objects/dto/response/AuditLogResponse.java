package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.AuditLog;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;

import java.time.OffsetDateTime;

public record AuditLogResponse(
        Long id,
        OffsetDateTime occurredAt,
        AuditAction action,
        Integer actorId,
        String actorEmail,
        String detail
) {
    public static AuditLogResponse from(AuditLog entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getOccurredAt(),
                entry.getAction(),
                entry.getActorId(),
                entry.getActorEmail(),
                entry.getDetail()
        );
    }
}
