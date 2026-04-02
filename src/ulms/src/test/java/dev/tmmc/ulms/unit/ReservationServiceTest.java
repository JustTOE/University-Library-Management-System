package dev.tmmc.ulms.unit;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        Reservation result = reservationService.cancelReservation(4);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
    }
}
