package dev.tmmc.ulms.objects.dto.response;

public record BookResponse(
        Integer id,
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        String subject,
        int totalCopies,
        int availableCopies,
        String shelfNumber
) {}
