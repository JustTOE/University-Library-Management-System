package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.PaymentRequest;
import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.objects.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService,
                             UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest request) {
        User user = userService.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userId()));
        return paymentService.processPaymentAsResponse(request.fineId(), request.method(), user);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(paymentService::findByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Integer id) {
        return paymentService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentService.findAllAsResponse();
    }
}
