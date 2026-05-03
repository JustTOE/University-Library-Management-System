package dev.tmmc.ulms.objects.dto.response;

import dev.tmmc.ulms.objects.entities.enums.FineStatus;

import java.math.BigDecimal;
import java.sql.Date;

public record FineResponse(
        Integer id,
        String fineId,
        Integer loanId,
        BigDecimal amount,
        Date calculatedDate,
        FineStatus status
) {}
