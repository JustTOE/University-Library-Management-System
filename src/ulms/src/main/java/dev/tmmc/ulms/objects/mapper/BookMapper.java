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
                b.getPublicationYear(),
                b.getSubject(),
                b.getTotalCopies(),
                b.getAvailableCopies(),
                b.getShelfNumber()
        );
    }

    public static Book toEntity(CreateBookRequest r) {
        Book b = new Book();
        b.setTitle(r.title());
        b.setAuthor(r.author());
        b.setIsbn(r.isbn());
        b.setPublicationYear(r.publicationYear());
        b.setSubject(r.subject());
        b.setTotalCopies(r.totalCopies());
        b.setAvailableCopies(r.availableCopies());
        b.setShelfNumber(r.shelfNumber());
        return b;
    }
}
