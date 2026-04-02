package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.LoanStatus;

import java.sql.Date;

public record LoanResponse(
        Integer id,
        String loanId,
        Integer userId,
        String userName,
        Integer bookId,
        String bookTitle,
        Date borrowDate,
        Date dueDate,
        Date returnDate,
        int renewalCount,
        LoanStatus status
) {}
