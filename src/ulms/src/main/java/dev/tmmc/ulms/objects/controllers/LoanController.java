package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.BorrowRequest;
import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.objects.services.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserService userService;

    public LoanController(LoanService loanService, UserService userService) {
        this.loanService = loanService;
        this.userService = userService;
    }

    @PostMapping("/borrow")
    public LoanResponse borrow(@Valid @RequestBody BorrowRequest request) {
        return loanService.borrowBookAsResponse(request.userId(), request.bookId());
    }

    @PutMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Integer id) {
        return loanService.returnBookAsResponse(id);
    }

    @PutMapping("/{id}/renew")
    public LoanResponse renew(@PathVariable Integer id) {
        return loanService.renewLoanAsResponse(id);
    }

    @GetMapping("/user/{userId}")
    public List<LoanResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(loanService::findByUserWithDetailsAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/paged")
    public Page<LoanResponse> getByUserPaged(@PathVariable Integer userId,
                                             Pageable pageable) {
        return userService.findById(userId)
                .map(user -> loanService.findByUserAsResponse(user, pageable))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Integer id) {
        return loanService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.findAllAsResponse();
    }
}
