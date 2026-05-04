package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.NotificationResponse;
import dev.tmmc.ulms.objects.dto.response.UnreadCountResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.NotificationService;
import dev.tmmc.ulms.objects.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService,
                                  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    public List<NotificationResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(notificationService::findByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    public UnreadCountResponse getUnreadCount(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> new UnreadCountResponse(notificationService.unreadCountFor(user)))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    public NotificationResponse getById(@PathVariable Integer id) {
        return notificationService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    public NotificationResponse acknowledge(@PathVariable Integer id) {
        return notificationService.markAcknowledgedAsResponse(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public List<NotificationResponse> getAll() {
        return notificationService.findAllAsResponse();
    }
}
