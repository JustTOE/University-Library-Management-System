package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.dto.response.ReservationResponse;
import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.ReservationService;
import dev.tmmc.ulms.support.TestFixtures;
import dev.tmmc.ulms.security.JwtPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservationPersistsActiveReservation() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(1, 0);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation reservation = reservationService.createReservation(1, 2);

        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertEquals(user, reservation.getUser());
        assertEquals(book, reservation.getBook());
        assertNotNull(reservation.getExpiry_date());
        assertNotNull(reservation.getReserved_at());
    }

    @Test
    void createReservationRejectsMissingUser() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(1, 2));
    }

    @Test
    void createReservationRejectsMissingBook() {
        when(userRepository.findById(1)).thenReturn(Optional.of(TestFixtures.user()));
        when(bookRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservationService.createReservation(1, 2));
    }

    @Test
    void cancelReservationMarksEntityAsCancelled() {
        Reservation reservation = TestFixtures.reservation(TestFixtures.user(), TestFixtures.book(1, 0), ReservationStatus.ACTIVE);
        when(reservationRepository.findById(4)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.cancelReservation(4);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private void seedPrincipal(Integer userId) {
        JwtPrincipal principal = new JwtPrincipal(userId, "test-" + userId + "@example.com", "STUDENT");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
    }

    @Test
    void findByUserAsResponseEnrichesQueuePositionForActiveReservations() {
        Book book = TestFixtures.book(1, 0);
        User user1 = TestFixtures.user();
        user1.setId(1);
        User user2 = TestFixtures.user();
        user2.setId(2);
        User user3 = TestFixtures.user();
        user3.setId(3);

        OffsetDateTime t1 = OffsetDateTime.now().minusMinutes(30);
        OffsetDateTime t2 = OffsetDateTime.now().minusMinutes(20);
        OffsetDateTime t3 = OffsetDateTime.now().minusMinutes(10);

        Reservation r1 = TestFixtures.reservation(user1, book, ReservationStatus.ACTIVE);
        r1.setReserved_at(t1);
        Reservation r2 = TestFixtures.reservation(user2, book, ReservationStatus.ACTIVE);
        r2.setReserved_at(t2);
        Reservation r3 = TestFixtures.reservation(user3, book, ReservationStatus.ACTIVE);
        r3.setReserved_at(t3);

        when(reservationRepository.findByUser(user1)).thenReturn(List.of(r1));
        when(reservationRepository.findByUser(user2)).thenReturn(List.of(r2));
        when(reservationRepository.findByUser(user3)).thenReturn(List.of(r3));
        when(reservationRepository.countActiveAheadOf(eq(book), eq(ReservationStatus.ACTIVE), eq(t1))).thenReturn(0L);
        when(reservationRepository.countActiveAheadOf(eq(book), eq(ReservationStatus.ACTIVE), eq(t2))).thenReturn(1L);
        when(reservationRepository.countActiveAheadOf(eq(book), eq(ReservationStatus.ACTIVE), eq(t3))).thenReturn(2L);

        assertEquals(1, reservationService.findByUserAsResponse(user1).get(0).queuePosition());
        assertEquals(2, reservationService.findByUserAsResponse(user2).get(0).queuePosition());
        assertEquals(3, reservationService.findByUserAsResponse(user3).get(0).queuePosition());
    }

    @Test
    void findByUserAsResponseReturnsNullQueuePositionForNonActiveReservation() {
        Book book = TestFixtures.book(1, 0);
        User user = TestFixtures.user();
        user.setId(1);
        Reservation cancelled = TestFixtures.reservation(user, book, ReservationStatus.CANCELLED);

        when(reservationRepository.findByUser(user)).thenReturn(List.of(cancelled));

        ReservationResponse response = reservationService.findByUserAsResponse(user).get(0);

        assertEquals(ReservationStatus.CANCELLED, response.status());
        assertNull(response.queuePosition());
    }

    @Test
    void findByIdAsResponseReturnsQueuePositionForActive() {
        Book book = TestFixtures.book(1, 0);
        User user = TestFixtures.user();
        user.setId(7);
        Reservation reservation = TestFixtures.reservation(user, book, ReservationStatus.ACTIVE);
        OffsetDateTime t = OffsetDateTime.now();
        reservation.setReserved_at(t);

        when(reservationRepository.findById(11)).thenReturn(Optional.of(reservation));
        when(reservationRepository.countActiveAheadOf(eq(book), eq(ReservationStatus.ACTIVE), eq(t))).thenReturn(2L);

        seedPrincipal(7);

        ReservationResponse response = reservationService.findByIdAsResponse(11).orElseThrow();

        assertEquals(3, response.queuePosition());
    }

    @Test
    void findByIdAsResponseReturnsNullQueuePositionForCancelled() {
        Book book = TestFixtures.book(1, 0);
        User user = TestFixtures.user();
        user.setId(7);
        Reservation reservation = TestFixtures.reservation(user, book, ReservationStatus.CANCELLED);

        when(reservationRepository.findById(12)).thenReturn(Optional.of(reservation));

        seedPrincipal(7);

        ReservationResponse response = reservationService.findByIdAsResponse(12).orElseThrow();

        assertNull(response.queuePosition());
    }
}
