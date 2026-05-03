package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.dto.response.ReservationResponse;
import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.mapper.ReservationMapper;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.security.OwnershipChecker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private static final int RESERVATION_EXPIRY_DAYS = 2;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Reservation createReservation(Integer userId, Integer bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReserved_at(OffsetDateTime.now());
        reservation.setExpiry_date(Date.valueOf(LocalDate.now().plusDays(RESERVATION_EXPIRY_DAYS)));
        reservation.setStatus(ReservationStatus.ACTIVE);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse createReservationAsResponse(Integer userId, Integer bookId) {
        return ReservationMapper.toResponse(createReservation(userId, bookId));
    }

    @Transactional
    public Reservation cancelReservation(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));
        OwnershipChecker.requireOwnerOrStaff(reservation.getUser().getId());
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservationAsResponse(Integer reservationId) {
        return ReservationMapper.toResponse(cancelReservation(reservationId));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByUser(User user) {
        return reservationRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByUserAsResponse(User user) {
        return reservationRepository.findByUser(user).stream()
                .map(ReservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Reservation> findByUser(User user, Pageable pageable) {
        return reservationRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Reservation> findById(Integer id) {
        return reservationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ReservationResponse> findByIdAsResponse(Integer id) {
        return reservationRepository.findById(id)
                .map(reservation -> {
                    OwnershipChecker.requireOwnerOrStaff(reservation.getUser().getId());
                    return ReservationMapper.toResponse(reservation);
                });
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllAsResponse() {
        return reservationRepository.findAll().stream()
                .map(ReservationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Reservation> findByBookAndStatus(Book book, ReservationStatus status) {
        return reservationRepository.findByBookAndStatusOrderByReservedAtAsc(book, status);
    }
}
