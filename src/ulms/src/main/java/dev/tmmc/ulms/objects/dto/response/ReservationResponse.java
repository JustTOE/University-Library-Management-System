package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;

import java.sql.Date;
import java.time.OffsetDateTime;

public record ReservationResponse(
        Integer id,
        Integer userId,
        String userName,
        Integer bookId,
        String bookTitle,
        OffsetDateTime reservedAt,
        Date expiryDate,
        ReservationStatus status,
        Integer queuePosition
) {}
