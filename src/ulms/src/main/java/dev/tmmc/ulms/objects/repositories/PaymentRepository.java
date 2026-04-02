package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByFine(Fine fine);

    List<Payment> findByUser(User user);

    Page<Payment> findByUser(User user, Pageable pageable);

    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
}
