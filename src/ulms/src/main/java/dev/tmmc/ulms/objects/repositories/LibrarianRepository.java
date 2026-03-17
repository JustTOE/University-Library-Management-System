package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.Librarian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibrarianRepository extends JpaRepository<Librarian, Long> {
    Optional<Librarian> findByStaffId(String staff_id);
    Optional<Librarian> findByEmail(String email);
    Optional<Librarian> findByName(String name);
}
