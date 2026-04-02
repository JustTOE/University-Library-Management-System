package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.LibraryCatalogResponse;
import dev.tmmc.ulms.objects.entities.LibraryCatalog;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.LibraryCatalogMapper;
import dev.tmmc.ulms.objects.services.LibraryCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class LibraryCatalogController {

    private final LibraryCatalogService libraryCatalogService;

    public LibraryCatalogController(LibraryCatalogService libraryCatalogService) {
        this.libraryCatalogService = libraryCatalogService;
    }

    @GetMapping
    public List<LibraryCatalogResponse> getAll() {
        return libraryCatalogService.findAll().stream()
                .map(LibraryCatalogMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public LibraryCatalogResponse getById(@PathVariable Integer id) {
        LibraryCatalog catalog = libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        return LibraryCatalogMapper.toResponse(catalog);
    }

    @PostMapping
    public LibraryCatalogResponse create(@RequestBody LibraryCatalog catalog) {
        return LibraryCatalogMapper.toResponse(libraryCatalogService.save(catalog));
    }

    @PutMapping("/{id}")
    public LibraryCatalogResponse update(@PathVariable Integer id,
                                         @RequestBody LibraryCatalog catalog) {
        libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        catalog.setId(id);
        return LibraryCatalogMapper.toResponse(libraryCatalogService.save(catalog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        libraryCatalogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
