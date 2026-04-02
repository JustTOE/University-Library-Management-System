package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.NotificationResponse;
import dev.tmmc.ulms.objects.entities.Notification;

public class NotificationMapper {

    private NotificationMapper() {}

    public static NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUser() != null ? n.getUser().getId() : null,
                n.getLoan() != null ? n.getLoan().getId() : null,
                n.getSent_date(),
                n.getMessage(),
                n.getType(),
                n.getStatus()
        );
    }
}
