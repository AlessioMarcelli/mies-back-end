package miesgroup.mies.webdev.Rest;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Service.EmailService;
import miesgroup.mies.webdev.Service.SessionService;

import java.util.HashMap;
import java.util.Map;

@Path("/email")
public class EmailResource {

    @Inject
    EmailService emailService;
    @Inject
    ClienteRepo clienteService;
    @Inject
    SessionService sessionService;
    @Inject
    Mailer mailer;

    @Path("/checkAlert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response checkAlert(@CookieParam("SESSION_COOKIE") int sessionId, JsonObject params) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);
        if (idUtente == 0) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessione non valida")
                    .build();
        }

        Map<String, Boolean> checkAlertStates = emailService.checkAlertStates(idUtente);
        String futureType = params.getString("futuresType");

        if (checkAlertStates.getOrDefault("MonthlyAlert", false) &&
                checkAlertStates.getOrDefault("QuarterlyAlert", false) &&
                checkAlertStates.getOrDefault("YearlyAlert", false)) {
            return Response.ok(futureType.equals("All") ? "ok" : "All").build();
        }
        if (checkAlertStates.getOrDefault("MonthlyAlert", false)) {
            return Response.ok(futureType.equals("Monthly") ? "ok" : "Monthly").build();
        }
        if (checkAlertStates.getOrDefault("QuarterlyAlert", false)) {
            return Response.ok(futureType.equals("Quarterly") ? "ok" : "Quarterly").build();
        }
        if (checkAlertStates.getOrDefault("YearlyAlert", false)) {
            return Response.ok(futureType.equals("Yearly") ? "ok" : "Yearly").build();
        }
        if (checkAlertStates.getOrDefault("GeneralAlert", false)) {
            return Response.ok(futureType.equals("General") ? "ok" : "General").build();
        }

        return Response.ok("Nessun alert attivo").build();
    }

    @Path("/checkAlertField")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkAlertField(@CookieParam("SESSION_COOKIE") Integer sessionId) {
        if (sessionId == null || sessionId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid session cookie.")
                    .build();
        }

        try {
            int idUtente = sessionService.trovaUtentebBySessione(sessionId);
            if (idUtente == 0) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida o scaduta.")
                        .build();
            }

            var alerts = emailService.checkUserAlertFillField(idUtente);
            var checkEmail = emailService.getCheckEmailStatus(idUtente);
            var response = new HashMap<>();
            response.put("alerts", alerts);
            response.put("checkEmail", checkEmail);

            return Response.ok(response).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while checking alerts.")
                    .build();
        }
    }

    @Path("/send-email")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendEmailToCliente(@CookieParam("SESSION_COOKIE") int sessionId, JsonObject params) {
        try {
            int idUtente = sessionService.trovaUtentebBySessione(sessionId);
            if (idUtente == 0) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Sessione non valida").build();
            }

            Cliente cliente = clienteService.findById(idUtente);
            if (cliente == null || cliente.getEmail() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Cliente o email non trovati").build();
            }

            String futuresType = params.getString("futuresType");
            boolean checkEmail = params.getBoolean("activeAlert");
            String corpoEmail = "Ciao " + cliente.getUsername() + ",\n\nLe tue informazioni sono state aggiornate:\n";

            if (futuresType.equals("All")) {
                double[] max = {
                        parseDoubleParam(params, "maximumLevelYearly"),
                        parseDoubleParam(params, "maximumLevelQuarterly"),
                        parseDoubleParam(params, "maximumLevelMonthly")
                };
                double[] min = {
                        parseDoubleParam(params, "minimumLevelYearly"),
                        parseDoubleParam(params, "minimumLevelQuarterly"),
                        parseDoubleParam(params, "minimumLevelMonthly")
                };
                String[] freq = {
                        params.getString("frequencyYearly"),
                        params.getString("frequencyQuarterly"),
                        params.getString("frequencyMonthly")
                };
                boolean[] checks = {
                        params.getBoolean("checkModalityYearly"),
                        params.getBoolean("checkModalityQuarterly"),
                        params.getBoolean("checkModalityMonthly")
                };
                for (int i = 0; i < freq.length; i++) {
                    corpoEmail += "Futures Type: " + (i == 0 ? "Yearly" : i == 1 ? "Quarterly" : "Monthly") + "\n";
                    corpoEmail += "Livello minimo: " + min[i] + (checks[i] ? "%" : "") + "\n";
                    corpoEmail += "Livello massimo: " + max[i] + (checks[i] ? "%" : "") + "\n";
                    corpoEmail += "Frequenza: " + freq[i] + "\n\n";
                }
                emailService.updateDataFuturesAlert(idUtente, futuresType, max, min, freq, checks, checkEmail);
            } else {
                double[] max = { parseDoubleParam(params, "maximumLevel") };
                double[] min = { parseDoubleParam(params, "minimumLevel") };
                String[] freq = { params.getString("frequencyAlert") };
                boolean[] checks = { params.getBoolean("checkModality") };

                corpoEmail += "Futures Type: " + futuresType + "\n";
                corpoEmail += "Livello minimo: " + min[0] + (checks[0] ? "%" : "") + "\n";
                corpoEmail += "Livello massimo: " + max[0] + (checks[0] ? "%" : "") + "\n";
                corpoEmail += "Frequenza: " + freq[0] + "\n";

                emailService.updateDataFuturesAlert(idUtente, futuresType, max, min, freq, checks, checkEmail);
            }

            Mail mail = Mail.withText(
                    cliente.getEmail(),
                    "Informazioni aggiornate",
                    corpoEmail
            );

            mailer.send(mail);
            return Response.ok("Email inviata con successo a " + cliente.getEmail()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante l'invio dell'email.").build();
        }
    }

    private double parseDoubleParam(JsonObject obj, String key) {
        if (!obj.containsKey(key)) return 0.0;
        var val = obj.get(key);
        return val instanceof JsonNumber ? ((JsonNumber) val).doubleValue() : Double.parseDouble(obj.getString(key));
    }

    @Path("/send-weekly-email")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendWeeklyEmail() {
        try {
            String recipient = "fiorenicolo.c@gmail.com";
            String subject = "Riepilogo Settimanale Mercato Energetico";

            Map<String, Double> settlementStart = Map.of(
                    "2/25", 145.32, "3/25", 156.20, "4/25", 147.09,
                    "1/26", 140.25, "2/26", 119.68, "3/26", 123.33, "4/26", 123.14
            );
            Map<String, Double> settlementEnd = Map.of(
                    "2/25", 130.05, "3/25", 143.04, "4/25", 138.23,
                    "1/26", 133.65, "2/26", 107.42, "3/26", 113.67, "4/26", 113.60
            );

            String body = buildEmailBody(settlementStart, settlementEnd);
            Mail mail = Mail.withHtml(recipient, subject, body);
            mailer.send(mail);

            return Response.ok("Email inviata con successo a " + recipient).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nell'invio dell'email: " + e.getMessage()).build();
        }
    }

    private String buildEmailBody(Map<String, Double> start, Map<String, Double> end) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>Ciao User1,</h2>")
                .append("<p>Questa settimana il mercato energetico ha avuto queste variazioni:</p>")
                .append("<h3>Dal giorno 10 febbraio 2025 al 14 febbraio 2025:</h3>");

        sb.append("<b>Quarter inizio settimana</b><table border='1'><tr><th>Anno/Mese</th><th>Settlement Price</th></tr>");
        for (var entry : start.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<b>Quarter fine settimana</b><table border='1'><tr><th>Anno/Mese</th><th>Settlement Price</th></tr>");
        for (var entry : end.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table>");

        sb.append("<b>Variazioni Percentuali</b><table border='1'><tr><th>Anno/Mese</th><th>Variazione %</th></tr>");
        for (var entry : start.entrySet()) {
            String key = entry.getKey();
            double variation = ((end.get(key) - entry.getValue()) / entry.getValue()) * 100;
            sb.append("<tr><td>").append(key).append("</td><td>").append(String.format("%.1f", variation)).append("%</td></tr>");
        }
        sb.append("</table>");

        return sb.toString();
    }
}