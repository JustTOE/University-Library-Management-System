package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.PaymentRequest;
import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.objects.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Charge fines through the payment gateway")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService,
                             UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT') and principal.userId == #request.userId()")
    @Operation(summary = "Charge a fine via the payment gateway; returns 402 if declined")
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest request) {
        User user = userService.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
        return paymentService.processPaymentAsResponse(request.fineId(), request.method(), user);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN') or principal.userId == #userId")
    @Operation(summary = "List a user's payments")
    public List<PaymentResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(paymentService::findByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','LIBRARIAN','ADMIN')")
    @Operation(summary = "Find a payment by id (owner or staff only)")
    public PaymentResponse getById(@PathVariable Integer id) {
        return paymentService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN') or hasRole('ADMIN')")
    @Operation(summary = "List every payment in the system (staff only)")
    public List<PaymentResponse> getAll() {
        return paymentService.findAllAsResponse();
    }
}
