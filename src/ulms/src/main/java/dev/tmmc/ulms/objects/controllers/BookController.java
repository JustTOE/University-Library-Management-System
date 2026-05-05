package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.CreateBookRequest;
import dev.tmmc.ulms.objects.dto.response.BookResponse;
import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.BookMapper;
import dev.tmmc.ulms.objects.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book catalog browsing, search, and CRUD")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all books with pagination")
    public Page<BookResponse> getAll(Pageable pageable) {
        return bookService.findAll(pageable).map(BookMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find a single book by id")
    public BookResponse getById(@PathVariable Integer id) {
        Book book = bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        return BookMapper.toResponse(book);
    }

    @GetMapping("/by-isbn")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find a single book by ISBN")
    public BookResponse getByIsbn(@RequestParam String isbn) {
        Book book = bookService.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        return BookMapper.toResponse(book);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search the catalog with optional title, author, and subject filters")
    public Page<BookResponse> search(@RequestParam(required = false) String title,
                                     @RequestParam(required = false) String author,
                                     @RequestParam(required = false) String subject,
                                     Pageable pageable) {
        return bookService.searchByFilters(title, author, subject, pageable)
                .map(BookMapper::toResponse);
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create a new book (librarian or admin)")
    public BookResponse create(@Valid @RequestBody CreateBookRequest request) {
        Book book = BookMapper.toEntity(request);
        return BookMapper.toResponse(bookService.save(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update an existing book (librarian or admin)")
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
    @Operation(summary = "Delete a book (rejected if it has active loans)")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        bookService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
