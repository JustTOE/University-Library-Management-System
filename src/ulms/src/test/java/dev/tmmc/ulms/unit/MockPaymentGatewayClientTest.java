package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.services.payment.MockPaymentGatewayClient;
import dev.tmmc.ulms.objects.services.payment.PaymentGatewayClient.ChargeResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockPaymentGatewayClientTest {

    private final MockPaymentGatewayClient client = new MockPaymentGatewayClient();

    @Test
    void declinesAmountBelowOneCent() {
        ChargeResult r = client.charge(new BigDecimal("0.005"), PaymentMethod.CARD, 1);
        assertFalse(r.success());
        assertEquals("AMOUNT_BELOW_MINIMUM", r.declineReason());
        assertNull(r.providerRef());
    }

    @Test
    void declinesCashOver100Euro() {
        ChargeResult r = client.charge(new BigDecimal("100.01"), PaymentMethod.CASH, 1);
        assertFalse(r.success());
        assertEquals("CASH_LIMIT_EXCEEDED", r.declineReason());
    }

    @Test
    void declinesUnsupportedMethod() {
        ChargeResult r = client.charge(new BigDecimal("5.00"), PaymentMethod.OTHER, 1);
        assertFalse(r.success());
        assertEquals("UNSUPPORTED_METHOD", r.declineReason());
    }

    @Test
    void acceptsCardForLargeAmount() {
        ChargeResult r = client.charge(new BigDecimal("5000.00"), PaymentMethod.CARD, 1);
        assertTrue(r.success());
        assertNotNull(r.providerRef());
        assertTrue(r.providerRef().startsWith("MOCK-"));
        assertNull(r.declineReason());
    }

    @Test
    void acceptsCashUpToOneHundredEuro() {
        ChargeResult r = client.charge(new BigDecimal("100.00"), PaymentMethod.CASH, 1);
        assertTrue(r.success());
    }
}
