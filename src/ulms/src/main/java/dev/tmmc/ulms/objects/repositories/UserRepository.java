package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUniversityId(String universityId);

    Optional<User> findByStaffId(String staffId);

    List<User> findByName(String name);
}
