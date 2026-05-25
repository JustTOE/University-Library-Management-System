package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.AuditService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    private UserService service() {
        return new UserService(userRepository, passwordEncoder, auditService);
    }

    /** A deactivated, locked-out user as it would exist in the DB before an edit. */
    private User lockedDeactivatedUser() {
        User user = TestFixtures.user();
        user.setId(7);
        user.setActive(false);
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(OffsetDateTime.now().plusMinutes(15));
        user.setPasswordHash("existing-hash");
        return user;
    }

    @Test
    void updatePreservesActiveAndLockoutState() {
        User existing = lockedDeactivatedUser();
        when(userRepository.findById(7)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service().update(7, "New Name", "new@example.com", "U-9", null, "0700000000",
                UserRole.STUDENT, null);

        // bug_007: editing the profile must NOT reactivate or unlock the account.
        assertFalse(existing.isActive(), "active must stay false");
        assertEquals(5, existing.getFailedLoginAttempts(), "failed attempts must be preserved");
        assertNotNull(existing.getLockedUntil(), "lockout must be preserved");
        // editable fields are applied
        assertEquals("New Name", existing.getName());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals(UserRole.STUDENT, existing.getRole());
    }

    @Test
    void updateWithBlankPasswordKeepsExistingHash() {
        User existing = lockedDeactivatedUser();
        when(userRepository.findById(7)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // bug_001: a blank/absent password on edit must not re-hash.
        service().update(7, "Name", "e@example.com", null, null, null, UserRole.STUDENT, "  ");
        service().update(7, "Name", "e@example.com", null, null, null, UserRole.STUDENT, null);

        assertEquals("existing-hash", existing.getPasswordHash());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateWithNewPasswordReHashes() {
        User existing = lockedDeactivatedUser();
        when(userRepository.findById(7)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("brand-new-pass")).thenReturn("new-hash");

        service().update(7, "Name", "e@example.com", null, null, null, UserRole.STUDENT, "brand-new-pass");

        assertEquals("new-hash", existing.getPasswordHash());
        verify(auditService).record(eq(AuditAction.USER_UPDATE), anyString());
    }


    @Test
    void registerHashesButDoesNotWriteUserCreateAudit() {
        User fresh = TestFixtures.user();
        when(passwordEncoder.encode("secret-pass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = service().register(fresh, "secret-pass");

        assertEquals("hashed", saved.getPasswordHash());
        // bug_015: registration path must not emit any audit row (REGISTER is
        // recorded by AuthService instead).
        verify(auditService, never()).record(any(AuditAction.class), anyString());
    }
}
