package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
            String title, String author, Pageable pageable);

    // Params are CAST to string so a NULL bind keeps a varchar type. Without the
    // cast PostgreSQL types an untyped NULL as bytea, and LOWER(bytea) does not
    // exist, so the whole statement fails at plan time even for non-null filters.
    @Query("SELECT b FROM Book b WHERE " +
           "(CAST(:title AS string) IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%'))) AND " +
           "(CAST(:author AS string) IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:author AS string), '%'))) AND " +
           "(CAST(:subject AS string) IS NULL OR b.subject = CAST(:subject AS string))")
    Page<Book> searchByFilters(
            @Param("title") String title,
            @Param("author") String author,
            @Param("subject") String subject,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies - 1 WHERE b.id = :id AND b.availableCopies > 0")
    int decrementAvailable(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.availableCopies = b.availableCopies + 1 WHERE b.id = :id")
    int incrementAvailable(@Param("id") Integer id);

    // Permanently removes one physical copy from inventory (used when a borrowed
    // copy is reported lost). The copy was already out, so availableCopies is
    // untouched — only the total shrinks.
    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.totalCopies = b.totalCopies - 1 WHERE b.id = :id AND b.totalCopies > 0")
    int decrementTotal(@Param("id") Integer id);
}
