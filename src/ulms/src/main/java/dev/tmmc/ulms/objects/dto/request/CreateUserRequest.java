package dev.tmmc.ulms.objects.dto.request;

import dev.tmmc.ulms.objects.dto.request.validation.OnCreate;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        String universityId,

        String staffId,

        String phone,

        @NotNull(message = "Role is required")
        UserRole role,

        // Required only on create (OnCreate group). On update a null/blank value
        // means "keep the existing password". The length rule applies whenever a
        // value is present (@Size skips nulls), so a supplied password is always
        // validated, on both create and update.
        @NotBlank(groups = OnCreate.class, message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}
