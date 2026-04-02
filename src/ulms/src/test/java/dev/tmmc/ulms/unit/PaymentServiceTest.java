package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentCreatesCompletedPaymentAndMarksFinePaid() {
        User user = TestFixtures.user();
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                FineStatus.UNPAID,
                BigDecimal.valueOf(12));

        when(fineRepository.findById(3)).thenReturn(Optional.of(fine));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.processPayment(3, PaymentMethod.CARD, user);

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(PaymentMethod.CARD, payment.getMethod());
        assertEquals(fine.getAmount(), payment.getAmount());
        assertEquals(FineStatus.PAID, fine.getStatus());
    }

    @Test
    void processPaymentRejectsAlreadyPaidFines() {
        User user = TestFixtures.user();
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                FineStatus.PAID,
                BigDecimal.ONE);

        when(fineRepository.findById(3)).thenReturn(Optional.of(fine));

        assertThrows(LoanStateException.class, () -> paymentService.processPayment(3, PaymentMethod.CASH, user));
    }

    @Test
    void processPaymentRejectsUnknownFineIds() {
        when(fineRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processPayment(3, PaymentMethod.ONLINE, TestFixtures.user()));
    }
}
