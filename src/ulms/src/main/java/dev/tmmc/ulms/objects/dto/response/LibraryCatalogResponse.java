package dev.tmmc.ulms.objects.dto.response;

import java.sql.Date;

public record LibraryCatalogResponse(
        Integer id,
        int totalBooks,
        Date lastUpdated
) {}
