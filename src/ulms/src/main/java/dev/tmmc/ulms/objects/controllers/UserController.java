package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.CreateUserRequest;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.UserMapper;
import dev.tmmc.ulms.objects.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-email")
    public UserResponse getByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-university-id")
    public UserResponse getByUniversityId(@RequestParam String universityId) {
        User user = userService.findByUniversityId(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with university ID: " + universityId));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-staff-id")
    public UserResponse getByStaffId(@RequestParam String staffId) {
        User user = userService.findByStaffId(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with staff ID: " + staffId));
        return UserMapper.toResponse(user);
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody CreateUserRequest request) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User user = UserMapper.toEntity(request);
        user.setId(id);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
