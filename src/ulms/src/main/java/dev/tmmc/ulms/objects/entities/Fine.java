package dev.tmmc.ulms.objects.entities;

import java.math.BigDecimal;
import java.sql.Date;
import jakarta.persistence.*;
import dev.tmmc.ulms.objects.entities.enums.*;

@Entity
@Table(name = "fine")
public class Fine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private BigDecimal amount;

    @Column(name = "fine_id", nullable = false, unique = true)
    private String fineId;

    private Date calculated_date;

    @Enumerated(EnumType.STRING)
    private FineStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    public Fine() {}

    public Fine(Integer id, BigDecimal amount, String fineId, Date calculated_date, FineStatus status, Loan loan) {
        this.id = id;
        this.amount = amount;
        this.fineId = fineId;
        this.calculated_date = calculated_date;
        this.status = status;
        this.loan = loan;
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

    public String getFineId() {
        return fineId;
    }

    public void setFineId(String fineId) {
        this.fineId = fineId;
    }

    public Date getCalculated_date() {
        return calculated_date;
    }

    public void setCalculated_date(Date calculated_date) {
        this.calculated_date = calculated_date;
    }

    public FineStatus getStatus() {
        return status;
    }

    public void setStatus(FineStatus status) {
        this.status = status;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }
}
