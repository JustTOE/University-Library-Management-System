package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.request.LoginRequest;
import dev.tmmc.ulms.objects.dto.request.RegisterRequest;
import dev.tmmc.ulms.objects.dto.response.AuthResponse;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.exceptions.AccountInactiveException;
import dev.tmmc.ulms.objects.exceptions.AccountLockedException;
import dev.tmmc.ulms.objects.exceptions.EmailAlreadyExistsException;
import dev.tmmc.ulms.objects.exceptions.InvalidCredentialsException;
import dev.tmmc.ulms.objects.mapper.UserMapper;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long jwtLifetimeMinutes;

    public AuthService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${ulms.security.jwt.lifetime-minutes:60}") long jwtLifetimeMinutes
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtLifetimeMinutes = jwtLifetimeMinutes;
    }

    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class, AccountInactiveException.class})
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new AccountInactiveException("Account is deactivated");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new AccountLockedException(
                    "Account locked due to too many failed login attempts",
                    user.getLockedUntil()
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                OffsetDateTime lockedUntil = now.plusMinutes(LOCKOUT_MINUTES);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(lockedUntil);
                userRepository.save(user);
                throw new AccountLockedException(
                        "Account locked due to too many failed login attempts",
                        lockedUntil
                );
            }
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        OffsetDateTime expiresAt = now.plusMinutes(jwtLifetimeMinutes);
        return new AuthResponse(token, expiresAt, user.getRole(), user.getId(), user.getName());
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setUniversityId(request.universityId());
        user.setPhone(request.phone());
        user.setRole(UserRole.STUDENT);
        user.setActive(true);

        return UserMapper.toResponse(userService.save(user, request.password()));
    }
}
