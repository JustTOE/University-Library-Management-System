package dev.tmmc.ulms.security;

import dev.tmmc.ulms.objects.entities.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class OwnershipChecker {

    private OwnershipChecker() {}

    public static void requireOwnerOrStaff(Integer ownerUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtPrincipal principal)) {
            return;
        }
        if (UserRole.LIBRARIAN.name().equals(principal.role())
                || UserRole.ADMIN.name().equals(principal.role())) {
            return;
        }
        if (ownerUserId == null || !ownerUserId.equals(principal.userId())) {
            throw new AccessDeniedException("You do not have access to this resource");
        }
    }
}
