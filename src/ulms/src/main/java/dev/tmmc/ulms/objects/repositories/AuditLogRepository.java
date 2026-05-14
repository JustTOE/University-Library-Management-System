package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.AuditLog;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:action IS NULL OR a.action = :action)
              AND (:actorId IS NULL OR a.actorId = :actorId)
            ORDER BY a.occurredAt DESC
            """)
    Page<AuditLog> findFiltered(
            @Param("action") AuditAction action,
            @Param("actorId") Integer actorId,
            Pageable pageable);
}
