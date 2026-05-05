package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.Collection;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Integer> {

    List<Loan> findByUserAndStatus(User user, LoanStatus status);

    boolean existsByBook_IdAndStatusIn(Integer bookId, Collection<LoanStatus> statuses);

    @Query("SELECT l FROM Loan l WHERE l.due_date < :date AND l.status <> :status")
    List<Loan> findOverdueLoansBefore(@Param("date") Date date, @Param("status") LoanStatus status);

    @Query("SELECT l FROM Loan l JOIN FETCH l.user JOIN FETCH l.book " +
            "WHERE l.due_date < :today AND l.status NOT IN " +
            "(dev.tmmc.ulms.objects.entities.enums.LoanStatus.RETURNED, " +
            " dev.tmmc.ulms.objects.entities.enums.LoanStatus.LOST)")
    List<Loan> findCandidatesForFineCalculation(@Param("today") Date today);

    @Query("SELECT l FROM Loan l JOIN FETCH l.book JOIN FETCH l.user WHERE l.user = :user")
    List<Loan> findByUserWithDetails(@Param("user") User user);

    Page<Loan> findByUser(User user, Pageable pageable);

    List<Loan> findByUser(User user);

    List<Loan> findByBook(Book book);
}
