package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.FineService;
import dev.tmmc.ulms.objects.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fines")
public class FineController {

    private final FineService fineService;
    private final UserService userService;

    public FineController(FineService fineService, UserService userService) {
        this.fineService = fineService;
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    public List<FineResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(fineService::findByLoanUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/unpaid")
    public List<FineResponse> getUnpaidByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(fineService::findUnpaidByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/user/{userId}/total-unpaid")
    public BigDecimal getTotalUnpaid(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> fineService.sumUnpaidByUser(user).orElse(BigDecimal.ZERO))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public FineResponse getById(@PathVariable Integer id) {
        return fineService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fine not found: " + id));
    }

    @PutMapping("/{id}/pay")
    public FineResponse markAsPaid(@PathVariable Integer id) {
        return fineService.markAsPaidAsResponse(id);
    }

    @GetMapping
    public List<FineResponse> getAll() {
        return fineService.findAllAsResponse();
    }
}
