package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.services.FineService;
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
class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @InjectMocks
    private FineService fineService;

    @Test
    void calculateFinePersistsExpectedAmountForOverdueLoan() {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(4));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Fine fine = fineService.calculateFine(loan);

        assertEquals(new BigDecimal("4.00"), fine.getAmount());
        assertEquals(FineStatus.UNPAID, fine.getStatus());
    }

    @Test
    void calculateFineRejectsNonOverdueLoan() {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.ACTIVE, LocalDate.now().plusDays(1));

        assertThrows(LoanStateException.class, () -> fineService.calculateFine(loan));
    }

    @Test
    void markAsPaidUpdatesFineStatus() {
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.ACTIVE, LocalDate.now().minusDays(2)),
                FineStatus.UNPAID,
                BigDecimal.TEN);

        when(fineRepository.findById(8)).thenReturn(Optional.of(fine));

        Fine result = fineService.markAsPaid(8);

        assertEquals(FineStatus.PAID, result.getStatus());
    }

    @Test
    void markAsPaidThrowsWhenFineIsMissing() {
        when(fineRepository.findById(8)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fineService.markAsPaid(8));
    }
}
