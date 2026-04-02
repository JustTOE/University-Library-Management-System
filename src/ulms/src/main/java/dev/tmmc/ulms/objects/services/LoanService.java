package dev.tmmc.ulms.objects.services;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.exceptions.BookNotAvailableException;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.ResourceNotFoundException;
import dev.tmmc.ulms.objects.exceptions.UnpaidFinesException;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private static final int MAX_RENEWALS = 3;
    private static final int LOAN_PERIOD_DAYS = 14;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("1.00");

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository,
                       FineRepository fineRepository,
                       ReservationRepository reservationRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Loan borrowBook(Integer userId, Integer bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));

        List<Fine> unpaidFines = fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID);
        if (!unpaidFines.isEmpty()) {
            throw new UnpaidFinesException("User has unpaid fines. Please clear fines before borrowing.");
        }

        int updated = bookRepository.decrementAvailable(bookId);
        if (updated == 0) {
            throw new BookNotAvailableException("Book is not available: " + book.getTitle());
        }

        Loan loan = new Loan();
        loan.setLoanId(UUID.randomUUID().toString());
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrow_date(Date.valueOf(LocalDate.now()));
        loan.setDue_date(Date.valueOf(LocalDate.now().plusDays(LOAN_PERIOD_DAYS)));
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setRenewal_count(0);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanStateException("Loan is already returned.");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturn_date(Date.valueOf(LocalDate.now()));
        bookRepository.incrementAvailable(loan.getBook().getId());

        List<Reservation> pendingReservations = reservationRepository
                .findByBookAndStatusOrderByReservedAtAsc(loan.getBook(), ReservationStatus.ACTIVE);
        if (!pendingReservations.isEmpty()) {
            Reservation next = pendingReservations.get(0);
            next.setStatus(ReservationStatus.FULFILLED);
        }

        LocalDate dueDate = loan.getDue_date().toLocalDate();
        if (LocalDate.now().isAfter(dueDate)) {
            long daysOverdue = LocalDate.now().toEpochDay() - dueDate.toEpochDay();
            BigDecimal fineAmount = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysOverdue));

            Fine fine = new Fine();
            fine.setFineId(UUID.randomUUID().toString());
            fine.setLoan(loan);
            fine.setAmount(fineAmount);
            fine.setCalculated_date(Date.valueOf(LocalDate.now()));
            fine.setStatus(FineStatus.UNPAID);
            fineRepository.save(fine);
        }

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan renewLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new LoanStateException("Cannot renew a returned loan.");
        }

        if (loan.getRenewal_count() >= MAX_RENEWALS) {
            throw new LoanStateException("Maximum number of renewals (" + MAX_RENEWALS + ") reached.");
        }

        List<Fine> unpaidFines = fineRepository.findByLoanUserAndStatusNot(loan.getUser(), FineStatus.PAID);
        if (!unpaidFines.isEmpty()) {
            throw new UnpaidFinesException("User has unpaid fines. Please clear fines before renewing.");
        }

        loan.setRenewal_count(loan.getRenewal_count() + 1);
        loan.setDue_date(Date.valueOf(loan.getDue_date().toLocalDate().plusDays(LOAN_PERIOD_DAYS)));
        loan.setStatus(LoanStatus.RENEWED);

        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public List<Loan> findByUser(User user) {
        return loanRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<Loan> findByUserWithDetails(User user) {
        return loanRepository.findByUserWithDetails(user);
    }

    @Transactional(readOnly = true)
    public List<Loan> findByUserAndStatus(User user, LoanStatus status) {
        return loanRepository.findByUserAndStatus(user, status);
    }

    @Transactional(readOnly = true)
    public Page<Loan> findByUser(User user, Pageable pageable) {
        return loanRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Loan> findById(Integer id) {
        return loanRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Loan> findAll() {
        return loanRepository.findAll();
    }
}
