package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.Payment;

public class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getFine() != null ? p.getFine().getId() : null,
                p.getUser() != null ? p.getUser().getId() : null,
                p.getAmount(),
                p.getPayment_date(),
                p.getMethod(),
                p.getStatus()
        );
    }
}
