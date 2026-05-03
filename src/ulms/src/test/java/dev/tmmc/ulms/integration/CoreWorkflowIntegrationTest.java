package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.repositories.BookRepository;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoreWorkflowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private FineRepository fineRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;
    @Autowired private UserService userService;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAllInBatch();
        fineRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        bookRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private User seedUser(UserRole role, String password) {
        User user = TestFixtures.user();
        user.setRole(role);
        return userService.save(user, password);
    }

    private String tokenFor(User user, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + user.getEmail() + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        int tokenStart = body.indexOf("\"token\":\"") + "\"token\":\"".length();
        int tokenEnd = body.indexOf('"', tokenStart);
        return body.substring(tokenStart, tokenEnd);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void borrowFlowPersistsLoanAndDecrementsAvailableCopies() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(2, 2));
        String token = tokenFor(student, "password1");

        mockMvc.perform(post("/api/loans/borrow")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + student.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(student.getId()))
                .andExpect(jsonPath("$.bookId").value(book.getId()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        assertEquals(1, updatedBook.getAvailableCopies());
        assertEquals(1, loanRepository.findAll().size());
    }

    @Test
    void returnFlowMarksLoanReturnedAndCreatesOverdueFine() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        User librarian = seedUser(UserRole.LIBRARIAN, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().minusDays(3)));
        String token = tokenFor(librarian, "password1");

        mockMvc.perform(put("/api/loans/{id}/return", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        Loan updatedLoan = loanRepository.findById(loan.getId()).orElseThrow();
        Book updatedBook = bookRepository.findById(book.getId()).orElseThrow();
        Fine generatedFine = fineRepository.findAll().get(0);

        assertEquals(LoanStatus.RETURNED, updatedLoan.getStatus());
        assertEquals(1, updatedBook.getAvailableCopies());
        assertEquals(FineStatus.UNPAID, generatedFine.getStatus());
        assertEquals(new BigDecimal("3.00"), generatedFine.getAmount());
    }

    @Test
    void paymentFlowCreatesPaymentAndMarksFinePaid() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(TestFixtures.loan(student, book, LoanStatus.OVERDUE, LocalDate.now().minusDays(2)));
        Fine fine = fineRepository.saveAndFlush(TestFixtures.fine(loan, FineStatus.UNPAID, new BigDecimal("2.00")));
        String token = tokenFor(student, "password1");

        mockMvc.perform(post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":" + fine.getId() + ",\"userId\":" + student.getId() + ",\"method\":\"CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.userId").value(student.getId()))
                .andExpect(jsonPath("$.fineId").value(fine.getId()));

        Fine updatedFine = fineRepository.findById(fine.getId()).orElseThrow();
        assertEquals(FineStatus.PAID, updatedFine.getStatus());
        assertEquals(1, paymentRepository.findAll().size());
    }

    @Test
    void reservationFlowCreatesAndCancelsReservation() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        String token = tokenFor(student, "password1");

        mockMvc.perform(post("/api/reservations")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + student.getId() + ",\"bookId\":" + book.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        int reservationId = reservationRepository.findAll().get(0).getId();

        mockMvc.perform(delete("/api/reservations/{id}", reservationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertEquals(1, reservationRepository.findAll().size());
        assertEquals(ReservationStatus.CANCELLED,
                reservationRepository.findById(reservationId).orElseThrow().getStatus());
    }
}
