package dev.tmmc.ulms.objects.services.payment;

import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGatewayClient {

    ChargeResult charge(BigDecimal amount, PaymentMethod method, Integer fineId);

    record ChargeResult(boolean success, String providerRef, String declineReason) {

        public static ChargeResult ok(String providerRef) {
            return new ChargeResult(true, providerRef, null);
        }

        public static ChargeResult declined(String reason) {
            return new ChargeResult(false, null, reason);
        }
    }
}
