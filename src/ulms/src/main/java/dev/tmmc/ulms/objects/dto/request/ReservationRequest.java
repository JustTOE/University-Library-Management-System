package dev.tmmc.ulms.objects.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "User ID is required")
        Integer userId,

        @NotNull(message = "Book ID is required")
        Integer bookId
) {}
