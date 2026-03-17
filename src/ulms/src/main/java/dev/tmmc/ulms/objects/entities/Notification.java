package dev.tmmc.ulms.objects.entities;

import java.time.OffsetDateTime;

public class Notification {
    private int id;
    private OffsetDateTime sent_date;
    private String message;
    private String type;
    private Integer loan_id;
    private int student_id;
    private String status;

    public Notification() {}

    public Notification(int id, OffsetDateTime sent_date, String message, String type, Integer loan_id, int student_id, String status) {
        this.id = id;
        this.sent_date = sent_date;
        this.message = message;
        this.type = type;
        this.loan_id = loan_id;
        this.student_id = student_id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLoan_id() {
        return loan_id;
    }

    public void setLoan_id(Integer loan_id) {
        this.loan_id = loan_id;
    }

    public int getStudent_id() {
        return student_id;
    }

    public void setStudent_id(int student_id) {
        this.student_id = student_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
