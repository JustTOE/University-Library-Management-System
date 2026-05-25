package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Test/debugging helpers, NOT for production. The whole bean is gated behind
 * {@code @Profile("!prod")} so it is never even instantiated under the prod
 * profile; the matching controller endpoint therefore returns 404 in prod
 * rather than relying solely on the role check.
 */
@Service
@Profile("!prod")
public class DebugService {

    private final LoanRepository loanRepository;
    private final FineService fineService;

    public DebugService(LoanRepository loanRepository, FineService fineService) {
        this.loanRepository = loanRepository;
        this.fineService = fineService;
    }

    /**
     * Backdate a loan's due date by {@code daysOverdue} days, flag it OVERDUE,
     * and create the matching UNPAID fine immediately so a payable fine appears
     * without waiting for the nightly job or returning the book.
     *
     * @return the fine that was created (or the existing UNPAID one if the loan
     *         was already overdue and fined).
     */
    @Transactional
    public Fine makeLoanOverdue(Integer loanId, int daysOverdue) {
        if (daysOverdue < 1) {
            throw new LoanStateException("daysOverdue must be at least 1.");
        }

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED || loan.getStatus() == LoanStatus.LOST) {
            throw new LoanStateException(
                    "Cannot make a " + loan.getStatus() + " loan overdue.");
        }

        loan.setDue_date(Date.valueOf(LocalDate.now().minusDays(daysOverdue)));
        loan.setStatus(LoanStatus.OVERDUE);
        loanRepository.save(loan);

        // calculateFine reads the (now backdated) due date and is idempotent:
        // it returns the existing UNPAID fine instead of stacking a second one.
        return fineService.calculateFine(loan);
    }
}
