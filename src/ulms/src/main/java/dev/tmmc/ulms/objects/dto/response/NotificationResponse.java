package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.NotificationStatus;
import dev.tmmc.ulms.objects.entities.enums.NotificationType;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Integer id,
        Integer userId,
        Integer loanId,
        OffsetDateTime sentDate,
        String message,
        NotificationType type,
        NotificationStatus status
) {}
