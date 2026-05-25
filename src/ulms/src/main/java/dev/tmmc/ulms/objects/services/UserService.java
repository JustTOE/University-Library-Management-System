package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUniversityId(String universityId) {
        return userRepository.findByUniversityId(universityId);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByStaffId(String staffId) {
        return userRepository.findByStaffId(staffId);
    }

    @Transactional
    public User save(User user, String rawPassword) {
        boolean isNew = user.getId() == null;
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        User saved = userRepository.save(user);
        auditService.record(
                isNew ? AuditAction.USER_CREATE : AuditAction.USER_UPDATE,
                "userId=" + saved.getId() + " email=" + saved.getEmail()
        );
        return saved;
    }

    /**
     * Persist a brand-new user with a hashed password but without writing a
     * USER_CREATE audit row. Used by the public self-registration path, which
     * records its own canonical REGISTER entry — avoids a duplicate, null-actor
     * USER_CREATE row since registration has no authenticated actor.
     */
    @Transactional
    public User register(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

    @Transactional
    public User save(User user) {
        boolean isNew = user.getId() == null;
        User saved = userRepository.save(user);
        auditService.record(
                isNew ? AuditAction.USER_CREATE : AuditAction.USER_UPDATE,
                "userId=" + saved.getId() + " email=" + saved.getEmail()
        );
        return saved;
    }

    /**
     * Update an existing user's editable profile fields on the managed row,
     * preserving account state the editor does not own — {@code active},
     * {@code failedLoginAttempts}, {@code lockedUntil}. The password is only
     * re-hashed when a non-blank {@code rawPassword} is supplied; a null/blank
     * value keeps the existing hash so a profile edit cannot lock a user out.
     */
    @Transactional
    public User update(Integer id, String name, String email, String universityId,
                       String staffId, String phone,
                       dev.tmmc.ulms.objects.entities.enums.UserRole role,
                       String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException("User not found: " + id));
        user.setName(name);
        user.setEmail(email);
        user.setUniversityId(universityId);
        user.setStaffId(staffId);
        user.setPhone(phone);
        user.setRole(role);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_UPDATE,
                "userId=" + saved.getId() + " email=" + saved.getEmail());
        return saved;
    }

    @Transactional
    public void deleteById(Integer id) {
        userRepository.deleteById(id);
        auditService.record(AuditAction.USER_UPDATE, "delete userId=" + id);
    }

    @Transactional
    public User activate(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException("User not found: " + id));
        user.setActive(true);
        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_ACTIVATE, "userId=" + id);
        return saved;
    }

    @Transactional
    public User deactivate(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException("User not found: " + id));
        user.setActive(false);
        User saved = userRepository.save(user);
        auditService.record(AuditAction.USER_DEACTIVATE, "userId=" + id);
        return saved;
    }

    /**
     * GDPR right-to-erasure: clear PII while preserving the row + foreign-key linkage
     * to historical loans/fines/payments. The original email/name/phone is replaced
     * with deterministic anonymised placeholders.
     */
    @Transactional
    public User anonymise(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException("User not found: " + id));
        String originalEmail = user.getEmail();
        String placeholder = "anonymised-user-" + id;
        user.setName(placeholder);
        user.setEmail(placeholder + "@deleted.invalid");
        user.setPhone(null);
        user.setUniversityId(null);
        user.setStaffId(null);
        user.setActive(false);
        user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        User saved = userRepository.save(user);
        auditService.record(AuditAction.ANONYMISE_USER,
                "targetUserId=" + id + " originalEmail=" + originalEmail);
        return saved;
    }

    public void recordExport(Integer id, String email) {
        auditService.record(AuditAction.EXPORT_USER, "userId=" + id + " email=" + email);
    }
}
