package dev.tmmc.ulms.objects.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import jakarta.persistence.*;
import dev.tmmc.ulms.objects.entities.enums.*;

@Entity
@Table(name = "payment")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private BigDecimal amount;
    private OffsetDateTime payment_date;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_id", nullable = false)
    private Fine fine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Payment() {}

    public Payment(Integer id, BigDecimal amount, OffsetDateTime payment_date, PaymentMethod method, PaymentStatus status, Fine fine) {
        this.id = id;
        this.amount = amount;
        this.payment_date = payment_date;
        this.method = method;
        this.status = status;
        this.fine = fine;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Fine getFine() {
        return fine;
    }

    public void setFine(Fine fine) {
        this.fine = fine;
    }
}
