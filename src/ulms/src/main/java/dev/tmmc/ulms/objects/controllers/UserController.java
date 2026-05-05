package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.CreateUserRequest;
import dev.tmmc.ulms.objects.dto.response.UserHistoryResponse;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.UserMapper;
import dev.tmmc.ulms.objects.services.FineService;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.objects.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User administration (admin only) and self-service profile lookup")
public class UserController {

    private final UserService userService;
    private final LoanService loanService;
    private final FineService fineService;

    public UserController(UserService userService,
                          LoanService loanService,
                          FineService fineService) {
        this.userService = userService;
        this.loanService = loanService;
        this.fineService = fineService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List every user (admin only)")
    public List<UserResponse> getAll() {
        return userService.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.userId == #id")
    @Operation(summary = "Find a user by id (self or admin only)")
    public UserResponse getById(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Find a user by email (staff only)")
    public UserResponse getByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-university-id")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Find a user by university ID (staff only)")
    public UserResponse getByUniversityId(@RequestParam String universityId) {
        User user = userService.findByUniversityId(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with university ID: " + universityId));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-staff-id")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Find a user by staff ID (staff only)")
    public UserResponse getByStaffId(@RequestParam String staffId) {
        User user = userService.findByStaffId(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with staff ID: " + staffId));
        return UserMapper.toResponse(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a user (any role, admin only)")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.userId == #id")
    @Operation(summary = "Update a user (self or admin only)")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody CreateUserRequest request) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User user = UserMapper.toEntity(request);
        user.setId(id);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user (admin only)")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate a deactivated user (admin only)")
    public UserResponse activate(@PathVariable Integer id) {
        return UserMapper.toResponse(userService.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a user (admin only)")
    public UserResponse deactivate(@PathVariable Integer id) {
        return UserMapper.toResponse(userService.deactivate(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Return a user's full loan and fine history (admin only)")
    public UserHistoryResponse history(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return new UserHistoryResponse(
                loanService.findByUserWithDetailsAsResponse(user),
                fineService.findByLoanUserAsResponse(user)
        );
    }
}
