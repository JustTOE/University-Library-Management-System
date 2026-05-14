package dev.tmmc.ulms.objects.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record UserExportResponse(
        OffsetDateTime exportedAt,
        UserResponse user,
        List<LoanResponse> loans,
        List<FineResponse> fines,
        List<ReservationResponse> reservations,
        List<PaymentResponse> payments,
        List<NotificationResponse> notifications
) {}
