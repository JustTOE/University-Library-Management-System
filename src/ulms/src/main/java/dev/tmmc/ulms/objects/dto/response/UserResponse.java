package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.UserRole;

public record UserResponse(
        Integer id,
        String name,
        String email,
        String universityId,
        String staffId,
        String phone,
        UserRole role
) {}
