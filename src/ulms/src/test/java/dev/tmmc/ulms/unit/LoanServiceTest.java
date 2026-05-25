package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.exceptions.BookNotAvailableException;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.exceptions.UnpaidFinesException;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setLostBookFee() {
        // @InjectMocks leaves the BigDecimal @Value field null (no matching mock);
        // set it to the configured default so reportLost math is exercised.
        ReflectionTestUtils.setField(loanService, "lostBookFee", new BigDecimal("50.00"));
    }

    @Test
    void borrowBookCreatesLoanWhenUserIsEligible() {
        User user = TestFixtures.user();
        user.setId(1);
        Book book = TestFixtures.book(3, 2);
        book.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        when(fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID)).thenReturn(List.of());
        when(bookRepository.decrementAvailable(2)).thenReturn(1);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.borrowBook(1, 2);

        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(LoanStatus.ACTIVE, result.getStatus());
        assertEquals(0, result.getRenewal_count());
        assertNotNull(result.getLoanId());
        assertNotNull(result.getBorrow_date());
        assertNotNull(result.getDue_date());
    }

    @Test
    void borrowBookRejectsUsersWithUnpaidFines() {
        User user = TestFixtures.user();
        user.setId(1);
        Book book = TestFixtures.book(2, 1);
        book.setId(2);
        Fine fine = TestFixtures.fine(TestFixtures.loan(user, book, LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                FineStatus.UNPAID, BigDecimal.TEN);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        when(fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID)).thenReturn(List.of(fine));

        assertThrows(UnpaidFinesException.class, () -> loanService.borrowBook(1, 2));

        verify(bookRepository, never()).decrementAvailable(2);
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void borrowBookRejectsWhenNoCopyCanBeReserved() {
        User user = TestFixtures.user();
        user.setId(1);
        Book book = TestFixtures.book(1, 0);
        book.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(bookRepository.findById(2)).thenReturn(Optional.of(book));
        when(fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID)).thenReturn(List.of());
        when(bookRepository.decrementAvailable(2)).thenReturn(0);

        assertThrows(BookNotAvailableException.class, () -> loanService.borrowBook(1, 2));

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnBookMarksLoanReturnedAndRestocksTheBook() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(1, 0);
        book.setId(4);
        Loan loan = TestFixtures.loan(user, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(2));
        loan.setId(9);

        when(loanRepository.findById(9)).thenReturn(Optional.of(loan));
        when(reservationRepository.findByBookAndStatusOrderByReservedAtAsc(book, ReservationStatus.ACTIVE)).thenReturn(List.of());
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.returnBook(9);

        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertNotNull(result.getReturn_date());
        verify(bookRepository).incrementAvailable(4);
        verify(fineRepository, never()).save(any(Fine.class));
    }

    @Test
    void returnBookCreatesFineWhenLoanIsOverdue() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(1, 0);
        book.setId(4);
        Loan loan = TestFixtures.loan(user, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(3));
        loan.setId(9);

        when(loanRepository.findById(9)).thenReturn(Optional.of(loan));
        when(reservationRepository.findByBookAndStatusOrderByReservedAtAsc(book, ReservationStatus.ACTIVE)).thenReturn(List.of());
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loanService.returnBook(9);

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(fineCaptor.capture());
        assertEquals(new BigDecimal("3.00"), fineCaptor.getValue().getAmount());
        assertEquals(FineStatus.UNPAID, fineCaptor.getValue().getStatus());
    }

    @Test
    void renewLoanExtendsDueDateAndIncrementsRenewalCount() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(1, 0);
        Loan loan = TestFixtures.loan(user, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(5));
        loan.setId(15);
        loan.setRenewal_count(1);

        when(loanRepository.findById(15)).thenReturn(Optional.of(loan));
        when(fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID)).thenReturn(List.of());
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.renewLoan(15);

        assertEquals(2, result.getRenewal_count());
        assertEquals(LoanStatus.RENEWED, result.getStatus());
        assertEquals(LocalDate.now().plusDays(19), result.getDue_date().toLocalDate());
    }

    @Test
    void renewLoanRejectsReturnedLoans() {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.RETURNED, LocalDate.now());
        loan.setId(15);

        when(loanRepository.findById(15)).thenReturn(Optional.of(loan));

        assertThrows(LoanStateException.class, () -> loanService.renewLoan(15));
    }

    @Test
    void renewLoanRejectsWhenMaximumRenewalsIsReached() {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.ACTIVE, LocalDate.now());
        loan.setId(15);
        loan.setRenewal_count(3);

        when(loanRepository.findById(15)).thenReturn(Optional.of(loan));

        assertThrows(LoanStateException.class, () -> loanService.renewLoan(15));
    }

    @Test
    void renewLoanRejectsWhenUserStillHasUnpaidFines() {
        User user = TestFixtures.user();
        Loan loan = TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.ACTIVE, LocalDate.now());
        loan.setId(15);

        when(loanRepository.findById(15)).thenReturn(Optional.of(loan));
        when(fineRepository.findByLoanUserAndStatusNot(user, FineStatus.PAID))
                .thenReturn(List.of(TestFixtures.fine(loan, FineStatus.UNPAID, BigDecimal.ONE)));

        assertThrows(UnpaidFinesException.class, () -> loanService.renewLoan(15));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void reportLostChargesFlatFeeWhenNotOverdue() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(3, 0);
        book.setId(4);
        Loan loan = TestFixtures.loan(user, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(2));
        loan.setId(9);

        when(loanRepository.findById(9)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Loan result = loanService.reportLost(9);

        assertEquals(LoanStatus.LOST, result.getStatus());
        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(fineCaptor.capture());
        assertEquals(new BigDecimal("50.00"), fineCaptor.getValue().getAmount());
        assertEquals(FineStatus.UNPAID, fineCaptor.getValue().getStatus());
        verify(bookRepository).decrementTotal(4);
        verify(bookRepository, never()).incrementAvailable(4);
    }

    @Test
    void reportLostAddsOverdueAmountToFlatFee() {
        User user = TestFixtures.user();
        Book book = TestFixtures.book(3, 0);
        book.setId(4);
        Loan loan = TestFixtures.loan(user, book, LoanStatus.OVERDUE, LocalDate.now().minusDays(3));
        loan.setId(9);

        when(loanRepository.findById(9)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(fineRepository.save(any(Fine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        loanService.reportLost(9);

        ArgumentCaptor<Fine> fineCaptor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(fineCaptor.capture());
        // €50 flat + €1/day * 3 days overdue = €53.00
        assertEquals(new BigDecimal("53.00"), fineCaptor.getValue().getAmount());
        verify(bookRepository).decrementTotal(4);
    }

    @Test
    void reportLostRejectsAlreadyClosedLoans() {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.RETURNED, LocalDate.now());
        loan.setId(9);

        when(loanRepository.findById(9)).thenReturn(Optional.of(loan));

        assertThrows(LoanStateException.class, () -> loanService.reportLost(9));
        verify(fineRepository, never()).save(any(Fine.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}
