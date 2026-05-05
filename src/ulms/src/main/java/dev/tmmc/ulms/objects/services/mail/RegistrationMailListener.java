package dev.tmmc.ulms.objects.services.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegistrationMailListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationMailListener.class);

    private final MailService mailService;

    public RegistrationMailListener(MailService mailService) {
        this.mailService = mailService;
    }

    @Async("ulmsTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRegistration(RegistrationCompletedEvent event) {
        try {
            mailService.sendRegistrationConfirmation(event.user());
        } catch (Exception ex) {
            log.warn("Registration confirmation mail failed for {}", event.user().getEmail(), ex);
        }
    }
}
