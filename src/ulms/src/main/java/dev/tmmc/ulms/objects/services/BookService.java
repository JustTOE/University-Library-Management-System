package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
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
        bookRepository.deleteById(id);
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
