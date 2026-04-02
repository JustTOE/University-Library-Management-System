package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.LibraryCatalogResponse;
import dev.tmmc.ulms.objects.entities.LibraryCatalog;

public class LibraryCatalogMapper {

    private LibraryCatalogMapper() {}

    public static LibraryCatalogResponse toResponse(LibraryCatalog c) {
        return new LibraryCatalogResponse(
                c.getId(),
                c.getTotal_books(),
                c.getLast_updated()
        );
    }
}
