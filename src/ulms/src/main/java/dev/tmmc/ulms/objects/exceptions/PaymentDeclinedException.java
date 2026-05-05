package dev.tmmc.ulms.objects.exceptions;

public class PaymentDeclinedException extends RuntimeException {

    private final String declineReason;

    public PaymentDeclinedException(String declineReason) {
        super("Payment was declined: " + declineReason);
        this.declineReason = declineReason;
    }

    public String getDeclineReason() {
        return declineReason;
    }
}
