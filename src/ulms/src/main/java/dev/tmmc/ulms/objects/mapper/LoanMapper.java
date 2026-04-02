package dev.tmmc.ulms.objects.mapper;

import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.entities.Loan;

public class LoanMapper {

    private LoanMapper() {}

    public static LoanResponse toResponse(Loan l) {
        return new LoanResponse(
                l.getId(),
                l.getLoanId(),
                l.getUser() != null ? l.getUser().getId() : null,
                l.getUser() != null ? l.getUser().getName() : null,
                l.getBook() != null ? l.getBook().getId() : null,
                l.getBook() != null ? l.getBook().getTitle() : null,
                l.getBorrow_date(),
                l.getDue_date(),
                l.getReturn_date(),
                l.getRenewal_count(),
                l.getStatus()
        );
    }
}
