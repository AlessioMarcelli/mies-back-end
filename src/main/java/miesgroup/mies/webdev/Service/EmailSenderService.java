package miesgroup.mies.webdev.Service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailSenderService {
    @Inject
    Mailer mailer;

    public void sendEmail(String to, String subject, String body) {
        // Crea una mail
        Mail mail = Mail.withText(to, subject, body);
        // Invia la mail
        mailer.send(mail);
    }
}
