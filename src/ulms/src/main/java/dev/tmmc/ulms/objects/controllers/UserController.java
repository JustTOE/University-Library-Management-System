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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
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
    public List<UserResponse> getAll() {
        return userService.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.userId == #id")
    public UserResponse getById(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-email")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public UserResponse getByEmail(@RequestParam String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-university-id")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public UserResponse getByUniversityId(@RequestParam String universityId) {
        User user = userService.findByUniversityId(universityId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with university ID: " + universityId));
        return UserMapper.toResponse(user);
    }

    @GetMapping("/by-staff-id")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    public UserResponse getByStaffId(@RequestParam String staffId) {
        User user = userService.findByStaffId(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with staff ID: " + staffId));
        return UserMapper.toResponse(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        User user = UserMapper.toEntity(request);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or principal.userId == #id")
    public UserResponse update(@PathVariable Integer id, @Valid @RequestBody CreateUserRequest request) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        User user = UserMapper.toEntity(request);
        user.setId(id);
        return UserMapper.toResponse(userService.save(user, request.password()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse activate(@PathVariable Integer id) {
        return UserMapper.toResponse(userService.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse deactivate(@PathVariable Integer id) {
        return UserMapper.toResponse(userService.deactivate(id));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public UserHistoryResponse history(@PathVariable Integer id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return new UserHistoryResponse(
                loanService.findByUserWithDetailsAsResponse(user),
                fineService.findByLoanUserAsResponse(user)
        );
    }
}
