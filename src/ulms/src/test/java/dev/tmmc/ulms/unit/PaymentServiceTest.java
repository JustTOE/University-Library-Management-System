package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.PaymentDeclinedException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.objects.services.payment.PaymentGatewayClient;
import dev.tmmc.ulms.objects.services.payment.PaymentGatewayClient.ChargeResult;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private PaymentGatewayClient gateway;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPaymentChargesGatewayBeforePersisting() {
        User user = TestFixtures.user();
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                FineStatus.UNPAID,
                BigDecimal.valueOf(12));

        when(fineRepository.findById(3)).thenReturn(Optional.of(fine));
        when(gateway.charge(eq(fine.getAmount()), eq(PaymentMethod.CARD), eq(3)))
                .thenReturn(ChargeResult.ok("MOCK-abc"));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.processPayment(3, PaymentMethod.CARD, user);

        verify(gateway, times(1)).charge(fine.getAmount(), PaymentMethod.CARD, 3);
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals("MOCK-abc", payment.getProviderRef());
        assertEquals(FineStatus.PAID, fine.getStatus());
    }

    @Test
    void processPaymentThrowsAndPersistsFailedRowWhenGatewayDeclines() {
        User user = TestFixtures.user();
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                FineStatus.UNPAID,
                new BigDecimal("250.00"));

        when(fineRepository.findById(3)).thenReturn(Optional.of(fine));
        when(gateway.charge(any(), eq(PaymentMethod.CASH), eq(3)))
                .thenReturn(ChargeResult.declined("CASH_LIMIT_EXCEEDED"));

        PaymentDeclinedException ex = assertThrows(PaymentDeclinedException.class,
                () -> paymentService.processPayment(3, PaymentMethod.CASH, user));

        assertEquals("CASH_LIMIT_EXCEEDED", ex.getDeclineReason());
        assertEquals(FineStatus.UNPAID, fine.getStatus());
        verify(fineRepository, never()).save(any(Fine.class));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.FAILED, captor.getValue().getStatus());
        assertEquals("CASH_LIMIT_EXCEEDED", captor.getValue().getDeclineReason());
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
        verify(gateway, never()).charge(any(), any(), any());
    }

    @Test
    void processPaymentRejectsUnknownFineIds() {
        when(fineRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.processPayment(3, PaymentMethod.ONLINE, TestFixtures.user()));
        verify(gateway, never()).charge(any(), any(), any());
    }
}
