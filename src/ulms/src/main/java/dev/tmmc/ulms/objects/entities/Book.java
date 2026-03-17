package dev.tmmc.ulms.objects.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class Book {
    private int id;
    private String title;
    private String author;
    private String isbn;
    private Integer publication_year;
    private String subject;
    private int total_copies;
    private int available_copies;
    private String shelf_number;

    public Book() {}

    public Book(int id, String title, String author, String isbn, Integer publication_year, String subject, int total_copies, int available_copies, String shelf_number) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publication_year = publication_year;
        this.subject = subject;
        this.total_copies = total_copies;
        this.available_copies = available_copies;
        this.shelf_number = shelf_number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPublication_year() {
        return publication_year;
    }

    public void setPublication_year(Integer publication_year) {
        this.publication_year = publication_year;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotal_copies() {
        return total_copies;
    }

    public void setTotal_copies(int total_copies) {
        this.total_copies = total_copies;
    }

    public int getAvailable_copies() {
        return available_copies;
    }

    public void setAvailable_copies(int available_copies) {
        this.available_copies = available_copies;
    }

    public String getShelf_number() {
        return shelf_number;
    }

    public void setShelf_number(String shelf_number) {
        this.shelf_number = shelf_number;
    }
}
