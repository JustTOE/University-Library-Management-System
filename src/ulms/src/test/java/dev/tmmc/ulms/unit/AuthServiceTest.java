package dev.tmmc.ulms.unit;

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
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.AuthService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService() {
        return new AuthService(userRepository, userService, passwordEncoder, jwtService, 60L);
    }

    private User activeUser() {
        User user = TestFixtures.user();
        user.setId(1);
        user.setActive(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        return user;
    }

    @Test
    void loginSuccessReturnsTokenAndResetsCounter() {
        User user = activeUser();
        user.setFailedLoginAttempts(2);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct", user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService().login(new LoginRequest(user.getEmail(), "correct"));

        assertEquals("jwt-token", response.token());
        assertEquals(UserRole.STUDENT, response.role());
        assertEquals(1, response.userId());
        assertNotNull(response.expiresAt());

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals(0, savedUser.getValue().getFailedLoginAttempts());
        assertNull(savedUser.getValue().getLockedUntil());
    }

    @Test
    void loginBadPasswordIncrementsCounter() {
        User user = activeUser();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService().login(new LoginRequest(user.getEmail(), "wrong")));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals(1, savedUser.getValue().getFailedLoginAttempts());
        assertNull(savedUser.getValue().getLockedUntil());
    }

    @Test
    void loginLocksAccountAfterFifthFailure() {
        User user = activeUser();
        user.setFailedLoginAttempts(4);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

        AccountLockedException ex = assertThrows(AccountLockedException.class,
                () -> authService().login(new LoginRequest(user.getEmail(), "wrong")));

        assertNotNull(ex.getLockedUntil());
        assertTrue(ex.getLockedUntil().isAfter(OffsetDateTime.now()));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals(0, savedUser.getValue().getFailedLoginAttempts());
        assertNotNull(savedUser.getValue().getLockedUntil());
    }

    @Test
    void loginRejectedWhileLocked() {
        User user = activeUser();
        OffsetDateTime lockedUntil = OffsetDateTime.now().plusMinutes(5);
        user.setLockedUntil(lockedUntil);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        AccountLockedException ex = assertThrows(AccountLockedException.class,
                () -> authService().login(new LoginRequest(user.getEmail(), "anything")));

        assertEquals(lockedUntil, ex.getLockedUntil());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginRejectedWhenInactive() {
        User user = activeUser();
        user.setActive(false);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(AccountInactiveException.class,
                () -> authService().login(new LoginRequest(user.getEmail(), "anything")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUnknownEmailThrowsInvalidCredentials() {
        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService().login(new LoginRequest("nope@example.com", "anything")));
    }

    @Test
    void registerCreatesStudentAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest(
                "Bob", "bob@example.com", "U-BOB", "0700000000", "secret-pass"
        );
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userService.save(any(User.class), any(String.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(42);
            return user;
        });

        UserResponse response = authService().register(request);

        assertEquals(42, response.id());
        assertEquals("Bob", response.name());
        assertEquals(UserRole.STUDENT, response.role());
        verify(userService, times(1)).save(any(User.class), any(String.class));
    }

    @Test
    void registerDuplicateEmailThrows() {
        RegisterRequest request = new RegisterRequest(
                "Bob", "bob@example.com", "U-BOB", null, "secret-pass"
        );
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(activeUser()));

        assertThrows(EmailAlreadyExistsException.class, () -> authService().register(request));
        verify(userService, never()).save(any(User.class), any(String.class));
    }
}
