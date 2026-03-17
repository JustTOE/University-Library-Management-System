package dev.tmmc.ulms.objects.entities;

import java.sql.Date;

public class LibraryCatalog {
    private int id;
    private int total_books;
    private Date last_updated;

    public LibraryCatalog() {}

    public LibraryCatalog(int id, int total_books, Date last_updated) {
        this.id = id;
        this.total_books = total_books;
        this.last_updated = last_updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
