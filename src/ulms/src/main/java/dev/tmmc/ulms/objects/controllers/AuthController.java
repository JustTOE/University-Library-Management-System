package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.LoginRequest;
import dev.tmmc.ulms.objects.dto.request.RegisterRequest;
import dev.tmmc.ulms.objects.dto.response.AuthResponse;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.UserMapper;
import dev.tmmc.ulms.objects.services.AuthService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, registration, and current-user endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user and issue a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new student account and trigger a confirmation email")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Return the currently authenticated user")
    public UserResponse me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new ResourceNotFoundException("Not authenticated");
        }
        User user = userService.findById(principal.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.userId()));
        return UserMapper.toResponse(user);
    }
}
