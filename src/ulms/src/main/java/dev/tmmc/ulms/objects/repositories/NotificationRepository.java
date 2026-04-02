package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Notification;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.NotificationStatus;
import dev.tmmc.ulms.objects.entities.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUser(User user);

    Page<Notification> findByUser(User user, Pageable pageable);

    List<Notification> findByLoan(Loan loan);

    List<Notification> findByUserAndType(User user, NotificationType type);

    List<Notification> findByUserAndStatus(User user, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.sent_date >= :from AND n.sent_date <= :to")
    List<Notification> findByUserAndSentDateBetween(
            @Param("user") User user,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}
