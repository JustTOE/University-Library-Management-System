package dev.tmmc.ulms.objects.entities;

import java.sql.Date;
import java.time.OffsetDateTime;

public class Reservation {
    private int id;
    private OffsetDateTime reserved_at;
    private Date expiry_date;
    private String status;
    private int student_id;
    private Integer book_id;

    public Reservation() {}

    public Reservation(int id, OffsetDateTime reserved_at, Date expiry_date, String status, int student_id, Integer book_id) {
        this.id = id;
        this.reserved_at = reserved_at;
        this.expiry_date = expiry_date;
        this.status = status;
        this.student_id = student_id;
        this.book_id = book_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public Integer getBook_id() {
        return book_id;
    }

    public void setBook_id(Integer book_id) {
        this.book_id = book_id;
    }
}
