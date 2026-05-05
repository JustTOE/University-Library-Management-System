package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.CreateLibraryCatalogRequest;
import dev.tmmc.ulms.objects.dto.response.LibraryCatalogResponse;
import dev.tmmc.ulms.objects.entities.LibraryCatalog;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.LibraryCatalogMapper;
import dev.tmmc.ulms.objects.services.LibraryCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Library Catalog", description = "Library catalog metadata (sections, sublocations)")
public class LibraryCatalogController {

    private final LibraryCatalogService libraryCatalogService;

    public LibraryCatalogController(LibraryCatalogService libraryCatalogService) {
        this.libraryCatalogService = libraryCatalogService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List every catalog entry")
    public List<LibraryCatalogResponse> getAll() {
        return libraryCatalogService.findAll().stream()
                .map(LibraryCatalogMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find a catalog entry by id")
    public LibraryCatalogResponse getById(@PathVariable Integer id) {
        LibraryCatalog catalog = libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        return LibraryCatalogMapper.toResponse(catalog);
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Create a new catalog entry (librarian or admin)")
    public LibraryCatalogResponse create(@Valid @RequestBody CreateLibraryCatalogRequest request) {
        LibraryCatalog catalog = LibraryCatalogMapper.toEntity(request);
        return LibraryCatalogMapper.toResponse(libraryCatalogService.save(catalog));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Update a catalog entry (librarian or admin)")
    public LibraryCatalogResponse update(@PathVariable Integer id,
                                         @Valid @RequestBody CreateLibraryCatalogRequest request) {
        libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        LibraryCatalog catalog = LibraryCatalogMapper.toEntity(request);
        catalog.setId(id);
        return LibraryCatalogMapper.toResponse(libraryCatalogService.save(catalog));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Delete a catalog entry (librarian or admin)")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        libraryCatalogService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catalog not found: " + id));
        libraryCatalogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
