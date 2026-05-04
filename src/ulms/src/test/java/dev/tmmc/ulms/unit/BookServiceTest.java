package dev.tmmc.ulms.unit;


import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.exceptions.BookInUseException;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.services.BookService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    BookRepository bookRepository;

    @Mock
    LoanRepository loanRepository;

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

    @Test
    void deleteByIdSucceedsWhenNoActiveLoans() {
        when(loanRepository.existsByBook_IdAndStatusIn(eq(7), any())).thenReturn(false);

        bookService.deleteById(7);

        verify(bookRepository).deleteById(7);
    }

    @Test
    void deleteByIdRejectsWhenActiveLoanExists() {
        when(loanRepository.existsByBook_IdAndStatusIn(eq(7), any())).thenReturn(true);

        BookInUseException ex = assertThrows(BookInUseException.class,
                () -> bookService.deleteById(7));

        assertTrue(ex.getMessage().contains("7"));
        verify(bookRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void deleteByIdChecksAgainstActiveRenewedAndOverdueStatuses() {
        when(loanRepository.existsByBook_IdAndStatusIn(eq(7), any())).thenReturn(false);

        bookService.deleteById(7);

        ArgumentCaptor<Collection<LoanStatus>> captor =
                ArgumentCaptor.forClass(Collection.class);
        verify(loanRepository).existsByBook_IdAndStatusIn(eq(7), captor.capture());
        assertEquals(EnumSet.of(LoanStatus.ACTIVE, LoanStatus.RENEWED, LoanStatus.OVERDUE),
                EnumSet.copyOf(captor.getValue()));
    }
}
