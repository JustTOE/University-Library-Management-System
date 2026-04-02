package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.FineController;
import dev.tmmc.ulms.objects.entities.Fine;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.services.FineService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FineController.class)
@AutoConfigureMockMvc(addFilters = false)
class FineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FineService fineService;

    @MockitoBean
    private UserService userService;

    @Test
    void getUnpaidByUserReturnsMappedFines() throws Exception {
        User user = TestFixtures.user();
        user.setId(1);
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(2)),
                FineStatus.UNPAID,
                new BigDecimal("5.00"));
        fine.setId(8);

        when(userService.findById(1)).thenReturn(Optional.of(user));
        when(fineService.findUnpaidByUser(user)).thenReturn(List.of(fine));

        mockMvc.perform(get("/api/fines/user/1/unpaid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(8))
                .andExpect(jsonPath("$[0].amount").value(5.00))
                .andExpect(jsonPath("$[0].status").value("UNPAID"));
    }

    @Test
    void getTotalUnpaidReturnsAggregateValue() throws Exception {
        User user = TestFixtures.user();
        when(userService.findById(1)).thenReturn(Optional.of(user));
        when(fineService.sumUnpaidByUser(user)).thenReturn(Optional.of(new BigDecimal("7.50")));

        mockMvc.perform(get("/api/fines/user/1/total-unpaid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(7.50));
    }

    @Test
    void markAsPaidReturnsUpdatedFine() throws Exception {
        Fine fine = TestFixtures.fine(
                TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(1)),
                FineStatus.PAID,
                BigDecimal.ONE);
        fine.setId(3);

        when(fineService.markAsPaid(3)).thenReturn(fine);

        mockMvc.perform(put("/api/fines/3/pay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void getUnpaidByUserReturns404WhenUserIsMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/fines/user/99/unpaid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void getByIdReturns404WhenFineIsMissing() throws Exception {
        when(fineService.findById(55)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/fines/55"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Fine not found: 55"));
    }
}
