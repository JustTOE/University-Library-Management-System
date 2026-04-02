package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.LibraryCatalog;
import dev.tmmc.ulms.objects.repositories.LibraryCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LibraryCatalogService {

    private final LibraryCatalogRepository libraryCatalogRepository;

    public LibraryCatalogService(LibraryCatalogRepository libraryCatalogRepository) {
        this.libraryCatalogRepository = libraryCatalogRepository;
    }

    @Transactional(readOnly = true)
    public List<LibraryCatalog> findAll() {
        return libraryCatalogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<LibraryCatalog> findById(Integer id) {
        return libraryCatalogRepository.findById(id);
    }

    @Transactional
    public LibraryCatalog save(LibraryCatalog catalog) {
        return libraryCatalogRepository.save(catalog);
    }

    @Transactional
    public void deleteById(Integer id) {
        libraryCatalogRepository.deleteById(id);
    }
}
