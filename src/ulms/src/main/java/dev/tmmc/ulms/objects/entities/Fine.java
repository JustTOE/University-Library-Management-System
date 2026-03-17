package dev.tmmc.ulms.objects.entities;

import java.math.BigDecimal;
import java.sql.Date;

public class Fine {
    private int id;
    private BigDecimal amount;
    private java.sql.Date calculated_date;
    private String status;
    private int days_overdue;
    private int loan_id;

    public Fine() {}

    public Fine(int id, BigDecimal amount, Date calculated_date, String status, int days_overdue, int loan_id) {
        this.id = id;
        this.amount = amount;
        this.calculated_date = calculated_date;
        this.status = status;
        this.days_overdue = days_overdue;
        this.loan_id = loan_id;
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

    public Date getCalculated_date() {
        return calculated_date;
    }

    public void setCalculated_date(Date calculated_date) {
        this.calculated_date = calculated_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDays_overdue() {
        return days_overdue;
    }

    public void setDays_overdue(int days_overdue) {
        this.days_overdue = days_overdue;
    }

    public int getLoan_id() {
        return loan_id;
    }

    public void setLoan_id(int loan_id) {
        this.loan_id = loan_id;
    }
}
