package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByStaffId(String staff_id);

    // In case someone has multiple admin accounts in their name
    List<Admin> findByName(String name);
}
