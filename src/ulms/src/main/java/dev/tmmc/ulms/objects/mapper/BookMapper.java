package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.request.CreateBookRequest;
import dev.tmmc.ulms.objects.dto.response.BookResponse;
import dev.tmmc.ulms.objects.entities.Book;

public class BookMapper {

    private BookMapper() {}

    public static BookResponse toResponse(Book b) {
        return new BookResponse(
                b.getId(),
                b.getTitle(),
                b.getAuthor(),
                b.getIsbn(),
                b.getPublication_year(),
                b.getSubject(),
                b.getTotal_copies(),
                b.getAvailable_copies(),
                b.getShelf_number()
        );
    }

    public static Book toEntity(CreateBookRequest r) {
        Book b = new Book();
        b.setTitle(r.title());
        b.setAuthor(r.author());
        b.setIsbn(r.isbn());
        b.setPublication_year(r.publicationYear());
        b.setSubject(r.subject());
        b.setTotal_copies(r.totalCopies());
        b.setAvailable_copies(r.availableCopies());
        b.setShelf_number(r.shelfNumber());
        return b;
    }
}
