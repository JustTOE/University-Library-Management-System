package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.mapper.FineMapper;
import dev.tmmc.ulms.objects.services.DebugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only debugging endpoints. The {@code @Profile("!prod")} on the bean means
 * it is not registered under the prod profile, so these routes 404 in
 * production. {@code @PreAuthorize} additionally restricts them to ADMINs in
 * every other profile.
 */
@RestController
@RequestMapping("/api/debug")
@Profile("!prod")
@Tag(name = "Debug", description = "Dev-only helpers for exercising flows (not available in prod)")
public class DebugController {

    private final DebugService debugService;

    public DebugController(DebugService debugService) {
        this.debugService = debugService;
    }

    @PostMapping("/loans/{id}/make-overdue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Backdate a loan's due date and create its overdue fine immediately (dev only)")
    public FineResponse makeLoanOverdue(@PathVariable Integer id,
                                        @RequestParam(defaultValue = "10") int days) {
        return FineMapper.toResponse(debugService.makeLoanOverdue(id, days));
    }
}
