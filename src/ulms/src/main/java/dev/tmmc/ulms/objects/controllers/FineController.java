package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.FineService;
import dev.tmmc.ulms.objects.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fines")
@Tag(name = "Fines", description = "Inspect and settle fines accrued from overdue loans")
public class FineController {

    private final FineService fineService;
    private final UserService userService;

    public FineController(FineService fineService, UserService userService) {
        this.fineService = fineService;
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "List every fine for a user")
    public List<FineResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(fineService::findByLoanUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/unpaid")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "List unpaid fines for a user")
    public List<FineResponse> getUnpaidByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(fineService::findUnpaidByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/total-unpaid")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "Sum of all unpaid fines for a user")
    public BigDecimal getTotalUnpaid(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> fineService.sumUnpaidByUser(user).orElse(BigDecimal.ZERO))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Find a fine by id (owner or staff only)")
    public FineResponse getById(@PathVariable Integer id) {
        return fineService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + id));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Mark a fine as paid (used by manual settlements; usually triggered by /api/payments)")
    public FineResponse markAsPaid(@PathVariable Integer id) {
        return fineService.markAsPaidAsResponse(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "List every fine in the system (staff only)")
    public List<FineResponse> getAll() {
        return fineService.findAllAsResponse();
    }
}
