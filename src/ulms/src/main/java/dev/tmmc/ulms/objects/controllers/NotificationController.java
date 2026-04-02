package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.NotificationResponse;
import dev.tmmc.ulms.objects.entities.Notification;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.NotificationMapper;
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
                .map(user -> notificationService.findByUser(user).stream()
                        .map(NotificationMapper::toResponse)
                        .toList())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable Integer id) {
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
        return NotificationMapper.toResponse(notification);
    }

    @PutMapping("/{id}/acknowledge")
    public NotificationResponse acknowledge(@PathVariable Integer id) {
        Notification notification = notificationService.markAcknowledged(id);
        return NotificationMapper.toResponse(notification);
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return notificationService.findAll().stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }
}
