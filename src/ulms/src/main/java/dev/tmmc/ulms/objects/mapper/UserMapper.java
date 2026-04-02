package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.request.CreateUserRequest;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;

public class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getUniversityId(),
                user.getStaffId(),
                user.getPhone(),
                user.getRole()
        );
    }

    public static User toEntity(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setUniversityId(request.universityId());
        user.setStaffId(request.staffId());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setPasswordHash(request.password());
        return user;
    }
}
