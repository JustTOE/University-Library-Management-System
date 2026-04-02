package dev.tmmc.ulms.objects.repositories;

import dev.tmmc.ulms.objects.entities.LibraryCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The repository shall be used in order to track total_books and last_updated.
 * Will extend with custom queries when reporting services require them.
 * May be removed though as the other repositories already implement the majority of the methods
 */
public interface LibraryCatalogRepository extends JpaRepository<LibraryCatalog, Integer> {

}
