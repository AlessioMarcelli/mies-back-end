package miesgroup.mies.webdev.Rest;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class EmailSenderResource {

    @Inject
    Mailer mailer;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/mail")
    @Produces(MediaType.TEXT_PLAIN)
    public String mail() {
        // Crea l'email
        Mail mail = Mail.withText(
                "fiorenicolo.c@gmail.com",  // Destinatario
                "Mail sent with Quarkus",  // Oggetto
                "Hello, This email has been sent with the imperative mailer."  // Corpo dell'email
        );

        // Invia l'email
        mailer.send(mail);

        return "Mail sent!";
    }
}