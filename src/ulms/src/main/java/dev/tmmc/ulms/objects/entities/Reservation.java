package dev.tmmc.ulms.objects.entities;

import java.sql.Date;
import java.time.OffsetDateTime;
import jakarta.persistence.*;
import dev.tmmc.ulms.objects.entities.enums.*;

@Entity
@Table(name = "reservation")
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private OffsetDateTime reserved_at;
    private Date expiry_date;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    public Reservation() {}

    public Reservation(Integer id, OffsetDateTime reserved_at, Date expiry_date, ReservationStatus status, User user, Book book) {
        this.id = id;
        this.reserved_at = reserved_at;
        this.expiry_date = expiry_date;
        this.status = status;
        this.user = user;
        this.book = book;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public OffsetDateTime getReserved_at() {
        return reserved_at;
    }

    public void setReserved_at(OffsetDateTime reserved_at) {
        this.reserved_at = reserved_at;
    }

    public Date getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(Date expiry_date) {
        this.expiry_date = expiry_date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
