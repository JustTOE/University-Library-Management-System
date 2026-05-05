package dev.tmmc.ulms.objects.services.payment;

import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

// Pinning note: CARD payments under EUR 100 must remain APPROVED so that
// CoreWorkflowIntegrationTest.paymentFlowCreatesPaymentAndMarksFinePaid keeps passing.
@Service
public class MockPaymentGatewayClient implements PaymentGatewayClient {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.01");
    private static final BigDecimal CASH_LIMIT = new BigDecimal("100.00");

    @Override
    public ChargeResult charge(BigDecimal amount, PaymentMethod method, Integer fineId) {
        if (amount == null || amount.compareTo(MIN_AMOUNT) < 0) {
            return ChargeResult.declined("AMOUNT_BELOW_MINIMUM");
        }
        if (method == PaymentMethod.OTHER) {
            return ChargeResult.declined("UNSUPPORTED_METHOD");
        }
        if (method == PaymentMethod.CASH && amount.compareTo(CASH_LIMIT) > 0) {
            return ChargeResult.declined("CASH_LIMIT_EXCEEDED");
        }
        return ChargeResult.ok("MOCK-" + UUID.randomUUID());
    }
}
