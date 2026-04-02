package dev.tmmc.ulms.objects.entities;

import java.sql.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "library_catalog")
public class LibraryCatalog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int total_books;
    private Date last_updated;

    public LibraryCatalog() {}

    public LibraryCatalog(Integer id, int total_books, Date last_updated) {
        this.id = id;
        this.total_books = total_books;
        this.last_updated = last_updated;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTotal_books() {
        return total_books;
    }

    public void setTotal_books(int total_books) {
        this.total_books = total_books;
    }

    public Date getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(Date last_updated) {
        this.last_updated = last_updated;
    }
}
