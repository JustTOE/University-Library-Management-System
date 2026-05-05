package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.services.mail.MailService;
import dev.tmmc.ulms.objects.services.mail.RegistrationCompletedEvent;
import dev.tmmc.ulms.objects.services.mail.RegistrationMailListener;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistrationMailListenerTest {

    @Mock
    private MailService mailService;

    @Test
    void delegatesToMailService() {
        RegistrationMailListener listener = new RegistrationMailListener(mailService);
        User user = TestFixtures.user();

        listener.onRegistration(new RegistrationCompletedEvent(user));

        verify(mailService, times(1)).sendRegistrationConfirmation(user);
    }

    @Test
    void swallowsMailFailures() {
        RegistrationMailListener listener = new RegistrationMailListener(mailService);
        User user = TestFixtures.user();
        doThrow(new RuntimeException("smtp boom")).when(mailService).sendRegistrationConfirmation(user);

        // No exception should escape — registration must not be impacted by mail failures.
        listener.onRegistration(new RegistrationCompletedEvent(user));

        verify(mailService).sendRegistrationConfirmation(user);
    }
}
