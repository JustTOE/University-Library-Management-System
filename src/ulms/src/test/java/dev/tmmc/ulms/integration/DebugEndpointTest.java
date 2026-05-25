package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the dev-only /api/debug helper. The "test" profile satisfies the
 * controller's {@code @Profile("!prod")} guard, so the endpoint is registered
 * here; a separate prod-profile run would 404 instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DebugEndpointTest {

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
    void makeOverdueBackdatesLoanAndCreatesUnpaidFine() throws Exception {
        User admin = seedUser(UserRole.ADMIN, "password1");
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        // Created not-yet-overdue (due in the future); the endpoint backdates it.
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(14)));
        String token = tokenFor(admin, "password1");

        mockMvc.perform(post("/api/debug/loans/{id}/make-overdue", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("days", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(loan.getId()))
                .andExpect(jsonPath("$.status").value("UNPAID"))
                .andExpect(jsonPath("$.amount").value(10.00));

        Loan updated = loanRepository.findById(loan.getId()).orElseThrow();
        assertEquals(LoanStatus.OVERDUE, updated.getStatus());
        assertEquals(LocalDate.now().minusDays(10), updated.getDue_date().toLocalDate());

        List<Fine> fines = fineRepository.findAll();
        assertEquals(1, fines.size());
        assertEquals(FineStatus.UNPAID, fines.get(0).getStatus());
        assertEquals(new BigDecimal("10.00"), fines.get(0).getAmount());
    }

    @Test
    void makeOverdueIsIdempotentAndDoesNotStackFines() throws Exception {
        User admin = seedUser(UserRole.ADMIN, "password1");
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(14)));
        String token = tokenFor(admin, "password1");

        mockMvc.perform(post("/api/debug/loans/{id}/make-overdue", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("days", "5"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/debug/loans/{id}/make-overdue", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("days", "5"))
                .andExpect(status().isOk());

        assertEquals(1, fineRepository.findAll().size());
    }

    @Test
    void makeOverdueForbiddenForNonAdmin() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(student, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(14)));
        String token = tokenFor(student, "password1");

        mockMvc.perform(post("/api/debug/loans/{id}/make-overdue", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("days", "10"))
                .andExpect(status().isForbidden());

        assertEquals(0, fineRepository.findAll().size());
    }
}
