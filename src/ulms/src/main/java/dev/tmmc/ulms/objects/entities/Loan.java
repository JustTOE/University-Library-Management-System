package dev.tmmc.ulms.objects.entities;

import java.sql.Date;

public class Loan {
    private int id;
    private Date borrow_date;
    private Date due_date;
    private Date return_date;
    private int renewal_count;
    private String status;
    private int student_id;
    private Integer book_id;

    public Loan() {}

    public Loan(int id, Date borrow_date, Date due_date, Date return_date, int renewal_count, String status, int student_id, Integer book_id) {
        this.id = id;
        this.borrow_date = borrow_date;
        this.due_date = due_date;
        this.return_date = return_date;
        this.renewal_count = renewal_count;
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

    public Date getBorrow_date() {
        return borrow_date;
    }

    public void setBorrow_date(Date borrow_date) {
        this.borrow_date = borrow_date;
    }

    public Date getDue_date() {
        return due_date;
    }

    public void setDue_date(Date due_date) {
        this.due_date = due_date;
    }

    public Date getReturn_date() {
        return return_date;
    }

    public void setReturn_date(Date return_date) {
        this.return_date = return_date;
    }

    public int getRenewal_count() {
        return renewal_count;
    }

    public void setRenewal_count(int renewal_count) {
        this.renewal_count = renewal_count;
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
