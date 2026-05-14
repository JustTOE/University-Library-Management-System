package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.exceptions.BookInUseException;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Optional;

@Service
public class BookService {

    private static final EnumSet<LoanStatus> BLOCKING_LOAN_STATUSES =
            EnumSet.of(LoanStatus.ACTIVE, LoanStatus.RENEWED, LoanStatus.OVERDUE);

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final AuditService auditService;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository, AuditService auditService) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }

    @Transactional(readOnly = true)
    public Page<Book> findByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Book> searchByFilters(String title, String author, String subject, Pageable pageable) {
        return bookRepository.searchByFilters(title, author, subject, pageable);
    }

    @Transactional
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteById(Integer id) {
        if (loanRepository.existsByBook_IdAndStatusIn(id, BLOCKING_LOAN_STATUSES)) {
            throw new BookInUseException(
                    "Book has active loans and cannot be deleted: " + id);
        }
        bookRepository.deleteById(id);
        auditService.record(AuditAction.BOOK_DELETE, "bookId=" + id);
    }

    @Transactional
    public int decrementAvailable(Integer bookId) {
        return bookRepository.decrementAvailable(bookId);
    }

    @Transactional
    public int incrementAvailable(Integer bookId) {
        return bookRepository.incrementAvailable(bookId);
    }
}
