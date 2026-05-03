package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.security.OwnershipChecker;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OwnershipCheckerTest {

    @AfterEach
    void clearPrincipal() {
        TestFixtures.clearPrincipal();
    }

    @Test
    void allowsResourceOwner() {
        TestFixtures.withPrincipal(UserRole.STUDENT, 7);
        assertDoesNotThrow(() -> OwnershipChecker.requireOwnerOrStaff(7));
    }

    @Test
    void rejectsCrossUserAccess() {
        TestFixtures.withPrincipal(UserRole.STUDENT, 7);
        assertThrows(AccessDeniedException.class,
                () -> OwnershipChecker.requireOwnerOrStaff(99));
    }

    @Test
    void allowsLibrarianBypass() {
        TestFixtures.withPrincipal(UserRole.LIBRARIAN, 1);
        assertDoesNotThrow(() -> OwnershipChecker.requireOwnerOrStaff(99));
    }

    @Test
    void allowsAdminBypass() {
        TestFixtures.withPrincipal(UserRole.ADMIN, 2);
        assertDoesNotThrow(() -> OwnershipChecker.requireOwnerOrStaff(99));
    }

    @Test
    void noAuthenticationIsTreatedAsBypass() {
        TestFixtures.clearPrincipal();
        assertDoesNotThrow(() -> OwnershipChecker.requireOwnerOrStaff(99));
    }
}
