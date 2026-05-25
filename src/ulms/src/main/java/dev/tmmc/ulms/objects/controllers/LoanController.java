package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.BorrowRequest;
import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.objects.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "Borrow, return, and renew loans")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('STUDENT') and principal.userId == #request.userId()")
    @Operation(summary = "Borrow a book for the current student")
    public LoanResponse borrow(@Valid @RequestBody BorrowRequest request) {
        return loanService.borrowBookAsResponse(request.userId(), request.bookId());
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "Mark a loan as returned (librarian or admin)")
    public LoanResponse returnBook(@PathVariable Integer id) {
        return loanService.returnBookAsResponse(id);
    }

    @PutMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Renew an active loan (subject to renewal cap and unpaid fines)")
    public LoanResponse renew(@PathVariable Integer id) {
        return loanService.renewLoanAsResponse(id);
    }

    @PutMapping("/{id}/report-lost")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Report a borrowed book as lost (owner or staff); raises a replacement fee")
    public LoanResponse reportLost(@PathVariable Integer id) {
        return loanService.reportLostAsResponse(id);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "List a user's loans with book and user details")
    public List<LoanResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(loanService::findByUserWithDetailsAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/paged")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "Paginated view of a user's loans")
    public Page<LoanResponse> getByUserPaged(@PathVariable Integer userId,
                                             Pageable pageable) {
        return userService.findById(userId)
                .map(user -> loanService.findByUserAsResponse(user, pageable))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Find a loan by id (owner or staff only)")
    public LoanResponse getById(@PathVariable Integer id) {
        return loanService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "List every loan in the system (staff only)")
    public List<LoanResponse> getAll() {
        return loanService.findAllAsResponse();
    }
}
