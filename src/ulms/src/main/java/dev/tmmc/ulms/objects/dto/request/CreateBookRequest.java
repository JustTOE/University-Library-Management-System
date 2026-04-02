package dev.tmmc.ulms.objects.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotBlank(message = "ISBN is required")
        @Size(max = 20, message = "ISBN must be at most 20 characters")
        String isbn,

        Integer publicationYear,

        String subject,

        @Min(value = 0, message = "Total copies must be non-negative")
        int totalCopies,

        @Min(value = 0, message = "Available copies must be non-negative")
        int availableCopies,

        String shelfNumber
) {}
