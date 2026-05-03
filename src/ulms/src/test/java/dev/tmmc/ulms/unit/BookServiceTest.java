package dev.tmmc.ulms.unit;


import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.services.BookService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    BookRepository bookRepository;

    @InjectMocks
    BookService bookService;

    @Test
    void findByIsbn() {
        Book book = TestFixtures.book(10, 8);
        String isbn = book.getIsbn();

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Optional<Book> found = bookService.findByIsbn(isbn);
        assertTrue(found.isPresent());
        assertEquals(isbn, found.get().getIsbn());
    }

    @Test
    void findByFilters() {
        Book book = TestFixtures.book(10, 8);
        String title = book.getTitle();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> expectedPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.searchByFilters(title, null, null, pageable)).thenReturn(expectedPage);

        Page<Book> result = bookService.searchByFilters(title, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(title, result.getContent().get(0).getTitle());
    }
}
