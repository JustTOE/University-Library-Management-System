package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.ReservationResponse;
import dev.tmmc.ulms.objects.entities.Reservation;

public class ReservationMapper {

    private ReservationMapper() {}

    public static ReservationResponse toResponse(Reservation r) {
        return toResponse(r, null);
    }

    public static ReservationResponse toResponse(Reservation r, Integer queuePosition) {
        return new ReservationResponse(
                r.getId(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getUser() != null ? r.getUser().getName() : null,
                r.getBook() != null ? r.getBook().getId() : null,
                r.getBook() != null ? r.getBook().getTitle() : null,
                r.getReserved_at(),
                r.getExpiry_date(),
                r.getStatus(),
                queuePosition
        );
    }
}
