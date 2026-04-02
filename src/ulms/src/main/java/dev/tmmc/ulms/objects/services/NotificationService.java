package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Notification;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.NotificationStatus;
import dev.tmmc.ulms.objects.entities.enums.NotificationType;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification createNotification(User user, Loan loan,
                                           NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setLoan(loan);
        notification.setType(type);
        notification.setMessage(message);
        notification.setSent_date(OffsetDateTime.now());
        notification.setStatus(NotificationStatus.NOT_ACKNOWLEDGED);

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAcknowledged(Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setStatus(NotificationStatus.ACKNOWLEDGED);
        return notification;
    }

    @Transactional(readOnly = true)
    public List<Notification> findByUser(User user) {
        return notificationRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findByUser(User user, Pageable pageable) {
        return notificationRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public List<Notification> findByLoan(Loan loan) {
        return notificationRepository.findByLoan(loan);
    }

    @Transactional(readOnly = true)
    public Optional<Notification> findById(Integer id) {
        return notificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }
}
