package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.UserRole;

import java.time.OffsetDateTime;

public record AuthResponse(
        String token,
        OffsetDateTime expiresAt,
        UserRole role,
        Integer userId,
        String name
) {}
