package dev.tmmc.ulms.support;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.entities.enums.UserRole;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User user() {
        User user = new User();
        user.setName("Alice Student");
        user.setEmail("alice+" + UUID.randomUUID() + "@example.com");
        user.setUniversityId("U" + UUID.randomUUID());
        user.setPhone("0712345678");
        user.setRole(UserRole.STUDENT);
        user.setPasswordHash("hashed-password");
        return user;
    }

    public static Book book(int totalCopies, int availableCopies) {
        Book book = new Book();
        book.setTitle("Distributed Systems");
        book.setAuthor("A. Author");
        String rawIsbn = UUID.randomUUID().toString().replace("-", "");
        book.setIsbn(("ISBN" + rawIsbn).substring(0, 16));
        book.setPublication_year(2024);
        book.setSubject("Computing");
        book.setTotal_copies(totalCopies);
        book.setAvailable_copies(availableCopies);
        book.setShelf_number("A1");
        return book;
    }

    public static Loan loan(User user, Book book, LoanStatus status, LocalDate dueDate) {
        Loan loan = new Loan();
        loan.setLoanId("LN-" + UUID.randomUUID());
        loan.setUser(user);
        loan.setBook(book);
        loan.setBorrow_date(Date.valueOf(dueDate.minusDays(14)));
        loan.setDue_date(Date.valueOf(dueDate));
        loan.setStatus(status);
        loan.setRenewal_count(0);
        return loan;
    }

    public static Fine fine(Loan loan, FineStatus status, BigDecimal amount) {
        Fine fine = new Fine();
        fine.setFineId("FN-" + UUID.randomUUID());
        fine.setLoan(loan);
        fine.setAmount(amount);
        fine.setCalculated_date(Date.valueOf(LocalDate.now()));
        fine.setStatus(status);
        return fine;
    }

    public static Reservation reservation(User user, Book book, ReservationStatus status) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReserved_at(OffsetDateTime.now());
        reservation.setExpiry_date(Date.valueOf(LocalDate.now().plusDays(2)));
        reservation.setStatus(status);
        return reservation;
    }

    public static Payment payment(Fine fine, User user, PaymentMethod method, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setFine(fine);
        payment.setUser(user);
        payment.setAmount(fine.getAmount());
        payment.setPayment_date(OffsetDateTime.now());
        payment.setMethod(method);
        payment.setStatus(status);
        return payment;
    }
}
