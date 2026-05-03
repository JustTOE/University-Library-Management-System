package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.entities.Fine;

public class FineMapper {

    private FineMapper() {}

    public static FineResponse toResponse(Fine f) {
        return new FineResponse(
                f.getId(),
                f.getFineId(),
                f.getLoan() != null ? f.getLoan().getId() : null,
                f.getAmount(),
                f.getCalculated_date(),
                f.getStatus()
        );
    }
}
