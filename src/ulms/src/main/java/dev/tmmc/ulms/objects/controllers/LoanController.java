package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.BorrowRequest;
import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.LoanMapper;
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
        Loan loan = loanService.borrowBook(request.userId(), request.bookId());
        return LoanMapper.toResponse(loan);
    }

    @PutMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Integer id) {
        Loan loan = loanService.returnBook(id);
        return LoanMapper.toResponse(loan);
    }

    @PutMapping("/{id}/renew")
    public LoanResponse renew(@PathVariable Integer id) {
        Loan loan = loanService.renewLoan(id);
        return LoanMapper.toResponse(loan);
    }

    @GetMapping("/user/{userId}")
    public List<LoanResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> loanService.findByUserWithDetails(user).stream()
                        .map(LoanMapper::toResponse)
                        .toList())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/paged")
    public Page<LoanResponse> getByUserPaged(@PathVariable Integer userId,
                                             Pageable pageable) {
        return userService.findById(userId)
                .map(user -> loanService.findByUser(user, pageable)
                        .map(LoanMapper::toResponse))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Integer id) {
        Loan loan = loanService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
        return LoanMapper.toResponse(loan);
    }

    @GetMapping
    public List<LoanResponse> getAll() {
        return loanService.findAll().stream()
                .map(LoanMapper::toResponse)
                .toList();
    }
}
