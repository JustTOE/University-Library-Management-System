package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CoreWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private FineRepository fineRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAllInBatch();
        fineRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void borrowFlowPersistsLoanAndDecrementsAvailableCopies() throws Exception {
        User user = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(2, 2));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + user.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.bookId").value(book.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(1, updatedBook.getAvailable_copies());
        assertEquals(1, loanRepository.findAll().size());
    }

    @Test
    void returnFlowMarksLoanReturnedAndCreatesOverdueFine() throws Exception {
        User user = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(TestFixtures.loan(user, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(3)));

        mockMvc.perform(put("/api/loans/{id}/return", loan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        Loan updatedLoan = loanRepository.findById(loan.getId()).orElseThrow();
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        Fine generatedFine = fineRepository.findAll().get(0);

        assertEquals(LoanStatus.RETURNED, updatedLoan.getStatus());
        assertEquals(1, updatedBook.getAvailable_copies());
        assertEquals(FineStatus.UNPAID, generatedFine.getStatus());
        assertEquals(new BigDecimal("3.00"), generatedFine.getAmount());
    }

    @Test
    void paymentFlowCreatesPaymentAndMarksFinePaid() throws Exception {
        User user = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(TestFixtures.loan(user, book, LoanStatus.OVERDUE, LocalDate.now().minusDays(2)));
        Fine fine = fineRepository.saveAndFlush(TestFixtures.fine(loan, FineStatus.UNPAID, new BigDecimal("2.00")));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":" + fine.getId() + ",\"userId\":" + user.getId() + ",\"method\":\"CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.fineId").value(fine.getId()));

        Fine updatedFine = fineRepository.findById(fine.getId()).orElseThrow();
        assertEquals(FineStatus.PAID, updatedFine.getStatus());
        assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void reservationFlowCreatesAndCancelsReservation() throws Exception {
        User user = userRepository.saveAndFlush(TestFixtures.user());
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + user.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        int reservationId = reservationRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/reservations/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(1, reservationRepository.findAll().size());
        assertEquals(ReservationStatus.CANCELLED,
                reservationRepository.findById(reservationId).orElseThrow().getStatus());
    }
}
