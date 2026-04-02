package dev.tmmc.ulms.objects.dto.request;

import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Fine ID is required")
        Integer fineId,

        @NotNull(message = "User ID is required")
        Integer userId,

        @NotNull(message = "Payment method is required")
        PaymentMethod method
) {}
