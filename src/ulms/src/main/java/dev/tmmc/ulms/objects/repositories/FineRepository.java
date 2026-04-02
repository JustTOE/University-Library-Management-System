package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Integer> {

    List<Fine> findByLoan(Loan loan);

    List<Fine> findByLoanUser(User user);

    List<Fine> findByLoanUserAndStatusNot(User user, FineStatus status);

    @Query("SELECT SUM(f.amount) FROM Fine f WHERE f.loan.user = :user AND f.status <> 'PAID'")
    Optional<BigDecimal> sumUnpaidByUser(@Param("user") User user);
}
