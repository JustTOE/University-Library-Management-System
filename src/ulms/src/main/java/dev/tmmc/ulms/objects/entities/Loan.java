package dev.tmmc.ulms.objects.entities;

import java.sql.Date;
import jakarta.persistence.*;
import dev.tmmc.ulms.objects.entities.enums.*;

@Entity
@Table(name = "loan")
public class Loan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Date borrow_date;
    private Date due_date;
    private Date return_date;

    @Column(name = "loan_id", nullable = false, unique = true)
    private String loanId;

    private int renewal_count;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    public Loan() {}

    public Loan(Integer id, Date borrow_date, Date due_date, Date return_date, String loanId, int renewal_count, LoanStatus status, User user, Book book) {
        this.id = id;
        this.borrow_date = borrow_date;
        this.due_date = due_date;
        this.return_date = return_date;
        this.loanId = loanId;
        this.renewal_count = renewal_count;
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

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public int getRenewal_count() {
        return renewal_count;
    }

    public void setRenewal_count(int renewal_count) {
        this.renewal_count = renewal_count;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
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
