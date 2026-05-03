package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.User;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

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
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void authenticatedStudentCanReadBooks() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        String token = tokenFor(student, "password1");

        mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void studentCannotCreateBook() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");
        String token = tokenFor(student, "password1");

        mockMvc.perform(post("/api/books")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"author\":\"y\",\"isbn\":\"ISBN0000000000\",\"publicationYear\":2024,\"subject\":\"s\",\"totalCopies\":1,\"availableCopies\":1,\"shelfNumber\":\"A1\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void librarianCanCreateBook() throws Exception {
        User librarian = seedUser(UserRole.LIBRARIAN, "password1");
        String token = tokenFor(librarian, "password1");

        mockMvc.perform(post("/api/books")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"x\",\"author\":\"y\",\"isbn\":\"ISBN1111111111\",\"publicationYear\":2024,\"subject\":\"s\",\"totalCopies\":1,\"availableCopies\":1,\"shelfNumber\":\"A1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("x"));
    }

    @Test
    void studentCannotRenewAnotherStudentsLoan() throws Exception {
        User owner = seedUser(UserRole.STUDENT, "password1");
        User other = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(owner, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(7)));
        String token = tokenFor(other, "password1");

        mockMvc.perform(put("/api/loans/{id}/renew", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCanRenewOwnLoan() throws Exception {
        User owner = seedUser(UserRole.STUDENT, "password1");
        Book book = bookRepository.saveAndFlush(TestFixtures.book(1, 0));
        Loan loan = loanRepository.saveAndFlush(
                TestFixtures.loan(owner, book, LoanStatus.ACTIVE, LocalDate.now().plusDays(7)));
        String token = tokenFor(owner, "password1");

        mockMvc.perform(put("/api/loans/{id}/renew", loan.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RENEWED"));
    }

    @Test
    void lockoutAfterFiveFailedLoginsReturns423() throws Exception {
        User student = seedUser(UserRole.STUDENT, "password1");

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"" + student.getEmail() + "\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + student.getEmail() + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isLocked())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.lockedUntil").exists());
    }

    @Test
    void seededAdminCanLogIn() throws Exception {
        // Re-create the admin row we just wiped — exercises that the V13 hash matches the documented password.
        User admin = new User();
        admin.setName("Default Admin");
        admin.setEmail("admin@ulms.local");
        admin.setRole(UserRole.ADMIN);
        admin.setPasswordHash("$2a$10$kDHxvn/UZmKbfKMusDCvcu78bEftor70gICk24Zcd0L00NOhKOp2.");
        admin.setActive(true);
        userRepository.saveAndFlush(admin);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@ulms.local\",\"password\":\"admin-change-me-now\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists());
    }
}
