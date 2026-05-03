package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.CreateBookRequest;
import dev.tmmc.ulms.objects.dto.response.BookResponse;
import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.BookMapper;
import dev.tmmc.ulms.objects.services.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<BookResponse> getAll(Pageable pageable) {
        return bookService.findAll(pageable).map(BookMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public BookResponse getById(@PathVariable Integer id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        return BookMapper.toResponse(book);
    }

    @GetMapping("/by-isbn")
    @PreAuthorize("isAuthenticated()")
    public BookResponse getByIsbn(@RequestParam String isbn) {
        Book book = bookService.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        return BookMapper.toResponse(book);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public Page<BookResponse> search(@RequestParam(required = false) String title,
                                     @RequestParam(required = false) String author,
                                     @RequestParam(required = false) String subject,
                                     Pageable pageable) {
        return bookService.searchByFilters(title, author, subject, pageable)
                .map(BookMapper::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        Book book = BookMapper.toEntity(request);
        return BookMapper.toResponse(bookService.save(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public BookResponse update(@PathVariable Integer id,
                               @Valid @RequestBody CreateBookRequest request) {
        bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        Book book = BookMapper.toEntity(request);
        book.setId(id);
        return BookMapper.toResponse(bookService.save(book));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
