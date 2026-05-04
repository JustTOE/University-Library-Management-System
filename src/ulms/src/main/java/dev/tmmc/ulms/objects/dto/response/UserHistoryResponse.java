package dev.tmmc.ulms.objects.dto.response;

import java.util.List;

public record UserHistoryResponse(
        List<LoanResponse> loans,
        List<FineResponse> fines
) {}
