package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.PaymentRequest;
import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.PaymentMapper;
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
        Payment payment = paymentService.processPayment(request.fineId(), request.method(), user);
        return PaymentMapper.toResponse(payment);
    }

    @GetMapping("/user/{userId}")
    public List<PaymentResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(user -> paymentService.findByUser(user).stream()
                        .map(PaymentMapper::toResponse)
                        .toList())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable Integer id) {
        Payment payment = paymentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
        return PaymentMapper.toResponse(payment);
    }

    @GetMapping
    public List<PaymentResponse> getAll() {
        return paymentService.findAll().stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }
}
