package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.request.CreateLibraryCatalogRequest;
import dev.tmmc.ulms.objects.dto.response.LibraryCatalogResponse;
import dev.tmmc.ulms.objects.entities.LibraryCatalog;

import java.sql.Date;
import java.time.LocalDate;

public class LibraryCatalogMapper {

    private LibraryCatalogMapper() {}

    public static LibraryCatalogResponse toResponse(LibraryCatalog c) {
        return new LibraryCatalogResponse(
                c.getId(),
                c.getTotalBooks(),
                c.getLastUpdated()
        );
    }

    public static LibraryCatalog toEntity(CreateLibraryCatalogRequest req) {
        LibraryCatalog c = new LibraryCatalog();
        c.setTotalBooks(req.totalBooks());
        c.setLastUpdated(req.lastUpdated() != null
                ? req.lastUpdated()
                : Date.valueOf(LocalDate.now()));
        return c;
    }
}
