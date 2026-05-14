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
