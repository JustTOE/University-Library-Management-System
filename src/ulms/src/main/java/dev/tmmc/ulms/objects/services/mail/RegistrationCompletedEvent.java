package dev.tmmc.ulms.objects.services.mail;

import dev.tmmc.ulms.objects.entities.User;

public record RegistrationCompletedEvent(User user) {
}
