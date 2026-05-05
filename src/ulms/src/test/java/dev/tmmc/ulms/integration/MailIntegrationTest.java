package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.repositories.FineRepository;
import dev.tmmc.ulms.objects.repositories.LoanRepository;
import dev.tmmc.ulms.objects.repositories.PaymentRepository;
import dev.tmmc.ulms.objects.repositories.ReservationRepository;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.mail.MailService;
import dev.tmmc.ulms.support.TestAsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class MailIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private FineRepository fineRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ReservationRepository reservationRepository;

    @MockitoBean
    private MailService mailService;

    @BeforeEach
    void cleanDatabase() {
        paymentRepository.deleteAllInBatch();
        fineRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        loanRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void registrationPublishesEventAndCallsMailServiceAfterCommit() throws Exception {
        String body = "{\"name\":\"Mail Tester\","
                + "\"email\":\"mail-test@example.com\","
                + "\"universityId\":\"U-MAIL\","
                + "\"phone\":\"0700000000\","
                + "\"password\":\"password1\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mailService, times(1)).sendRegistrationConfirmation(captor.capture());
        assertEquals("mail-test@example.com", captor.getValue().getEmail());
    }
}
