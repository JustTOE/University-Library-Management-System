package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.PaymentController;
import dev.tmmc.ulms.objects.dto.response.PaymentResponse;
import dev.tmmc.ulms.objects.entities.Payment;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.FineStatus;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.entities.enums.PaymentMethod;
import dev.tmmc.ulms.objects.entities.enums.PaymentStatus;
import dev.tmmc.ulms.objects.exceptions.LoanStateException;
import dev.tmmc.ulms.objects.mapper.PaymentMapper;
import dev.tmmc.ulms.objects.services.PaymentService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private UserService userService;

    @Test
    void processPaymentReturnsMappedPayment() throws Exception {
        User user = TestFixtures.user();
        user.setId(1);
        Payment payment = TestFixtures.payment(
                TestFixtures.fine(
                        TestFixtures.loan(user, TestFixtures.book(1, 0), LoanStatus.OVERDUE, LocalDate.now().minusDays(3)),
                        FineStatus.PAID,
                        new BigDecimal("9.00")),
                user,
                PaymentMethod.CARD,
                PaymentStatus.COMPLETED);
        payment.setId(4);
        PaymentResponse response = PaymentMapper.toResponse(payment);

        when(userService.findById(1)).thenReturn(Optional.of(user));
        when(paymentService.processPaymentAsResponse(2, PaymentMethod.CARD, user)).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":2,\"userId\":1,\"method\":\"CARD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.method").value("CARD"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void processPaymentRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":2,\"userId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.method").value("Payment method is required"));
    }

    @Test
    void processPaymentReturns404WhenUserIsMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":2,\"userId\":99,\"method\":\"CASH\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void processPaymentReturnsConflictWhenFineIsAlreadyPaid() throws Exception {
        User user = TestFixtures.user();
        when(userService.findById(1)).thenReturn(Optional.of(user));
        when(paymentService.processPaymentAsResponse(2, PaymentMethod.CARD, user))
                .thenThrow(new LoanStateException("Fine is already paid."));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fineId\":2,\"userId\":1,\"method\":\"CARD\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Fine is already paid."));
    }

    @Test
    void getByIdReturns404WhenPaymentIsMissing() throws Exception {
        when(paymentService.findByIdAsResponse(45)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/45"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found: 45"));
    }
}
