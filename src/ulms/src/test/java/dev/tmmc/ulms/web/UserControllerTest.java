package dev.tmmc.ulms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tmmc.ulms.objects.controllers.UserController;
import dev.tmmc.ulms.objects.dto.request.CreateUserRequest;
import dev.tmmc.ulms.objects.dto.response.FineResponse;
import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.services.FineService;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.objects.services.NotificationService;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.objects.services.ReservationService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private UserService userService;
    @MockitoBean private LoanService loanService;
    @MockitoBean private FineService fineService;
    @MockitoBean private ReservationService reservationService;
    @MockitoBean private PaymentService paymentService;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private JwtService jwtService;

    @BeforeEach
    void setUpPrincipal() {
        TestFixtures.withPrincipal(UserRole.ADMIN, 99);
    }

    @AfterEach
    void clearPrincipal() {
        TestFixtures.clearPrincipal();
    }

    private User existing(int id) {
        User user = TestFixtures.user();
        user.setId(id);
        return user;
    }

    @Test
    void getAllReturnsList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(existing(1), existing(2)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByIdReturnsUser() throws Exception {
        when(userService.findById(1)).thenReturn(Optional.of(existing(1)));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void createReturnsMappedUser() throws Exception {
        when(userService.save(any(User.class), eq("password1"))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(5);
            return user;
        });

        CreateUserRequest request = new CreateUserRequest(
                "Carol", "carol@example.com", null, "S-1", null, UserRole.LIBRARIAN, "password1"
        );
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));
    }

    @Test
    void deleteAnonymisesUserAndReturnsCleared() throws Exception {
        User anonymised = existing(3);
        anonymised.setName("anonymised-user-3");
        anonymised.setEmail("anonymised-user-3@deleted.invalid");
        anonymised.setActive(false);
        when(userService.anonymise(3)).thenReturn(anonymised);

        mockMvc.perform(delete("/api/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("anonymised-user-3"))
                .andExpect(jsonPath("$.email").value("anonymised-user-3@deleted.invalid"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void exportReturnsFullDump() throws Exception {
        User user = existing(11);
        when(userService.findById(11)).thenReturn(Optional.of(user));
        when(loanService.findByUserWithDetailsAsResponse(user)).thenReturn(List.of());
        when(fineService.findByLoanUserAsResponse(user)).thenReturn(List.of());
        when(reservationService.findByUserAsResponse(user)).thenReturn(List.of());
        when(paymentService.findByUserAsResponse(user)).thenReturn(List.of());
        when(notificationService.findByUserAsResponse(user)).thenReturn(List.of());

        mockMvc.perform(get("/api/users/11/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(11))
                .andExpect(jsonPath("$.loans").isArray())
                .andExpect(jsonPath("$.fines").isArray())
                .andExpect(jsonPath("$.reservations").isArray())
                .andExpect(jsonPath("$.payments").isArray())
                .andExpect(jsonPath("$.notifications").isArray())
                .andExpect(jsonPath("$.exportedAt").exists());
    }

    @Test
    void exportReturns404WhenUserMissing() throws Exception {
        when(userService.findById(404)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/404/export"))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateReturnsActiveUser() throws Exception {
        User user = existing(4);
        user.setActive(true);
        when(userService.activate(4)).thenReturn(user);

        mockMvc.perform(put("/api/users/4/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void deactivateReturnsDeactivatedUser() throws Exception {
        User user = existing(4);
        user.setActive(false);
        when(userService.deactivate(4)).thenReturn(user);

        mockMvc.perform(put("/api/users/4/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void createReturnsAdminWhenAdminRoleRequested() throws Exception {
        when(userService.save(any(User.class), eq("password1"))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(8);
            return user;
        });

        CreateUserRequest request = new CreateUserRequest(
                "Daria", "daria@example.com", null, "S-2", null, UserRole.ADMIN, "password1"
        );
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void historyReturnsCombinedLoansAndFines() throws Exception {
        User user = existing(11);
        when(userService.findById(11)).thenReturn(Optional.of(user));

        LoanResponse loan = new LoanResponse(
                1, "LN-1", 11, "Alice Student", 2, "Distributed Systems",
                Date.valueOf(LocalDate.now().minusDays(30)),
                Date.valueOf(LocalDate.now().minusDays(16)),
                Date.valueOf(LocalDate.now().minusDays(15)),
                0, LoanStatus.RETURNED);
        FineResponse fine = new FineResponse(
                1, "FN-1", 1, new BigDecimal("1.00"),
                Date.valueOf(LocalDate.now().minusDays(15)), FineStatus.PAID);

        when(loanService.findByUserWithDetailsAsResponse(user)).thenReturn(List.of(loan));
        when(fineService.findByLoanUserAsResponse(user)).thenReturn(List.of(fine));

        mockMvc.perform(get("/api/users/11/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loans.length()").value(1))
                .andExpect(jsonPath("$.loans[0].loanId").value("LN-1"))
                .andExpect(jsonPath("$.loans[0].status").value("RETURNED"))
                .andExpect(jsonPath("$.fines.length()").value(1))
                .andExpect(jsonPath("$.fines[0].fineId").value("FN-1"))
                .andExpect(jsonPath("$.fines[0].status").value("PAID"));
    }

    @Test
    void historyReturns404WhenUserMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }
}
