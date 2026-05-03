package dev.tmmc.ulms.objects.dto.request;

import jakarta.validation.constraints.Min;

import java.sql.Date;

public record CreateLibraryCatalogRequest(
        @Min(value = 0, message = "Total books must be non-negative")
        int totalBooks,

        Date lastUpdated
) {}
