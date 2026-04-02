package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT r FROM Reservation r WHERE r.book = :book AND r.status = :status ORDER BY r.reserved_at ASC")
    List<Reservation> findByBookAndStatusOrderByReservedAtAsc(@Param("book") Book book, @Param("status") ReservationStatus status);

    List<Reservation> findByUser(User user);

    Page<Reservation> findByUser(User user, Pageable pageable);
}
