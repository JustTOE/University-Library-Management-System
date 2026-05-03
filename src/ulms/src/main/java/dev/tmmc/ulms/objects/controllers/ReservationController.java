package dev.tmmc.ulms.objects.controllers;

import dev.tmmc.ulms.objects.dto.request.ReservationRequest;
import dev.tmmc.ulms.objects.dto.response.ReservationResponse;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.services.ReservationService;
import dev.tmmc.ulms.objects.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    public ReservationController(ReservationService reservationService,
                                 UserService userService) {
        this.reservationService = reservationService;
        this.userService = userService;
    }

    @PostMapping
    public ReservationResponse create(@Valid @RequestBody ReservationRequest request) {
        return reservationService.createReservationAsResponse(request.userId(), request.bookId());
    }

    @DeleteMapping("/{id}")
    public ReservationResponse cancel(@PathVariable Integer id) {
        return reservationService.cancelReservationAsResponse(id);
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> getByUser(@PathVariable Integer userId) {
        return userService.findById(userId)
                .map(reservationService::findByUserAsResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable Integer id) {
        return reservationService.findByIdAsResponse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + id));
    }

    @GetMapping
    public List<ReservationResponse> getAll() {
        return reservationService.findAllAsResponse();
    }
}
