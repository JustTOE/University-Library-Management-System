package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.services.mail.MailService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void buildsExpectedSmtpMessage() {
        MailService service = new MailService(mailSender, "no-reply@ulms.local", true);
        User user = TestFixtures.user();
        user.setName("Alice");
        user.setEmail("alice@example.com");

        service.sendRegistrationConfirmation(user);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("no-reply@ulms.local", message.getFrom());
        assertNotNull(message.getTo());
        assertEquals(1, message.getTo().length);
        assertEquals("alice@example.com", message.getTo()[0]);
        assertEquals("Welcome to ULMS", message.getSubject());
        assertNotNull(message.getText());
        assertTrue(message.getText().contains("Alice"));
        assertTrue(message.getText().contains("alice@example.com"));
    }

    @Test
    void skipsSendWhenDisabled() {
        MailService service = new MailService(mailSender, "no-reply@ulms.local", false);
        User user = TestFixtures.user();

        service.sendRegistrationConfirmation(user);

        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }
}
