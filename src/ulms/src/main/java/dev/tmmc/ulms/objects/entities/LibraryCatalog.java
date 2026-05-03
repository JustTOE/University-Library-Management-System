package dev.tmmc.ulms.objects.entities;

import java.sql.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "library_catalog")
public class LibraryCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "total_books", nullable = false)
    private int totalBooks;

    @Column(name = "last_updated", nullable = false)
    private Date lastUpdated;

    public LibraryCatalog() {}

    public LibraryCatalog(Integer id, int totalBooks, Date lastUpdated) {
        this.id = id;
        this.totalBooks = totalBooks;
        this.lastUpdated = lastUpdated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
