package dev.tmmc.ulms.objects.entities;

import java.time.OffsetDateTime;
import jakarta.persistence.*;
import dev.tmmc.ulms.objects.entities.enums.*;

@Entity
@Table(name = "notification")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private OffsetDateTime sent_date;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    public Notification() {}

    public Notification(Integer id, OffsetDateTime sent_date, String message, NotificationType type, Loan loan, User user, NotificationStatus status) {
        this.id = id;
        this.sent_date = sent_date;
        this.message = message;
        this.type = type;
        this.loan = loan;
        this.user = user;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OffsetDateTime getSent_date() {
        return sent_date;
    }

    public void setSent_date(OffsetDateTime sent_date) {
        this.sent_date = sent_date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }
}
