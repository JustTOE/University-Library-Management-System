package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book,Long> {
    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);

    // Send the data in chunks for better
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.available_copies = b.available_copies - 1 WHERE b.id = :id AND b.available_copies > 0")
    int decrementAvailable(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.available_copies = b.available_copies + 1 WHERE b.id = :id")
    int incrementAvailable(@Param("id") Long id);
}
