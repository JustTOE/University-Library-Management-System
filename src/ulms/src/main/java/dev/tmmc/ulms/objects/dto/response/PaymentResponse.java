package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Integer id,
        Integer fineId,
        Integer userId,
        BigDecimal amount,
        OffsetDateTime paymentDate,
        PaymentMethod method,
        PaymentStatus status
) {}
