package dev.tmmc.ulms.objects.services.mail;

import dev.tmmc.ulms.objects.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public MailService(JavaMailSender mailSender,
                       @Value("${ulms.mail.from:no-reply@ulms.local}") String from,
                       @Value("${ulms.mail.enabled:true}") boolean enabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
    }

    public void sendRegistrationConfirmation(User user) {
        if (!enabled) {
            log.debug("mail disabled; skipping registration confirmation for {}", user.getEmail());
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(user.getEmail());
        message.setSubject("Welcome to ULMS");
        message.setText("Hi " + user.getName() + ",\n\n"
                + "Your ULMS account is ready. You can sign in with the email "
                + user.getEmail() + ".\n\n"
                + "University Library Management System");
        mailSender.send(message);
    }
}
