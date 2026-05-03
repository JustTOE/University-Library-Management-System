package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.NotificationResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.NotificationService;
import dev.tmmc.ulms.objects.services.UserService;
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
    public List<NotificationResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(notificationService::findByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable Integer id) {
        return notificationService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }

    @PutMapping("/{id}/acknowledge")
    public NotificationResponse acknowledge(@PathVariable Integer id) {
        return notificationService.markAcknowledgedAsResponse(id);
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationService.findAllAsResponse();
    }
}
