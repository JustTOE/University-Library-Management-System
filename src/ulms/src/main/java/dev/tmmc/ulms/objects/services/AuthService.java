package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.request.LoginRequest;
import dev.tmmc.ulms.objects.dto.request.RegisterRequest;
import dev.tmmc.ulms.objects.dto.response.AuthResponse;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.exceptions.AccountInactiveException;
import dev.tmmc.ulms.objects.exceptions.AccountLockedException;
import dev.tmmc.ulms.objects.exceptions.EmailAlreadyExistsException;
import dev.tmmc.ulms.objects.exceptions.InvalidCredentialsException;
import dev.tmmc.ulms.objects.mapper.UserMapper;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.mail.RegistrationCompletedEvent;
import dev.tmmc.ulms.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final long jwtLifetimeMinutes;

    public AuthService(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApplicationEventPublisher eventPublisher,
            AuditService auditService,
            @Value("${ulms.security.jwt.lifetime-minutes:60}") long jwtLifetimeMinutes
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
        this.jwtLifetimeMinutes = jwtLifetimeMinutes;
    }

    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class, AccountInactiveException.class})
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);
        if (user == null) {
            auditService.record(AuditAction.LOGIN_FAILURE, null, request.email(), "unknown email");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            auditService.record(AuditAction.LOGIN_FAILURE, user.getId(), user.getEmail(), "account inactive");
            throw new AccountInactiveException("Account is deactivated");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            auditService.record(AuditAction.LOGIN_LOCKED, user.getId(), user.getEmail(),
                    "locked until " + user.getLockedUntil());
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
                auditService.record(AuditAction.LOGIN_LOCKED, user.getId(), user.getEmail(),
                        "locked after " + MAX_FAILED_ATTEMPTS + " failed attempts");
                throw new AccountLockedException(
                        "Account locked due to too many failed login attempts",
                        lockedUntil
                );
            }
            user.setFailedLoginAttempts(attempts);
            userRepository.save(user);
            auditService.record(AuditAction.LOGIN_FAILURE, user.getId(), user.getEmail(),
                    "bad password (attempt " + attempts + " of " + MAX_FAILED_ATTEMPTS + ")");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        OffsetDateTime expiresAt = now.plusMinutes(jwtLifetimeMinutes);
        auditService.record(AuditAction.LOGIN_SUCCESS, user.getId(), user.getEmail(), null);
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

        User saved = userService.register(user, request.password());
        eventPublisher.publishEvent(new RegistrationCompletedEvent(saved));
        auditService.record(AuditAction.REGISTER, saved.getId(), saved.getEmail(), "self-registration");
        return UserMapper.toResponse(saved);
    }
}
