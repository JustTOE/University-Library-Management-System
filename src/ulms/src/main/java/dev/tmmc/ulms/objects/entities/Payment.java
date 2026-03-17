package dev.tmmc.ulms.objects.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Payment {
    private int id;
    private BigDecimal amount;
    private OffsetDateTime payment_date;
    private String method;
    private String status;
    private int fine_id;

    public Payment() {}

    public Payment(int id, BigDecimal amount, OffsetDateTime payment_date, String method, String status, int fine_id) {
        this.id = id;
        this.amount = amount;
        this.payment_date = payment_date;
        this.method = method;
        this.status = status;
        this.fine_id = fine_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OffsetDateTime getPayment_date() {
        return payment_date;
    }

    public void setPayment_date(OffsetDateTime payment_date) {
        this.payment_date = payment_date;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getFine_id() {
        return fine_id;
    }

    public void setFine_id(int fine_id) {
        this.fine_id = fine_id;
    }
}
