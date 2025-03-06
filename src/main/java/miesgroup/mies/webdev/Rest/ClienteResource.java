package miesgroup.mies.webdev.Rest;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.AlertData;
import miesgroup.mies.webdev.Persistance.Model.AlertResponse;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Service.ClienteService;
import miesgroup.mies.webdev.Service.EmailScheduler;
import miesgroup.mies.webdev.Service.SessionService;

import java.util.Map;

@Path("/cliente")
public class ClienteResource {

    private final ClienteService clienteService;
    private final SessionService sessionService;

    @Inject
    Mailer mailer;

    @Inject
    EmailScheduler emailScheduler;

    public ClienteResource(ClienteService clienteService, SessionService sessionService) {
        this.clienteService = clienteService;
        this.sessionService = sessionService;
    }

    // Recupera il cliente basato sul sessionId
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCliente(@CookieParam("SESSION_COOKIE") int sessionId) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);
        Cliente cliente = clienteService.getCliente(idUtente);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cliente non trovato")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // Aggiorna le informazioni del cliente
    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCliente(@CookieParam("SESSION_COOKIE") int sessionId, Map<String, String> updateData) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);

        if (idUtente == 0) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessione non valida")
                    .build();
        }

        for (Map.Entry<String, String> entry : updateData.entrySet()) {
            String field = entry.getKey();
            String newValue = entry.getValue();

            boolean isUpdated = clienteService.updateCliente(idUtente, field, newValue);
            if (!isUpdated) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Aggiornamento fallito per il campo: " + field)
                        .build();
            }
        }

        return Response.ok().build();
    }

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

        // Ottieni lo stato degli alert
        Map<String, Boolean> checkAlertStates = clienteService.checkAlertStates(idUtente);

        // Recupera il futureType dalla richiesta
        String futureType = params.getString("futuresType");

        // Verifica gli stati degli alert
        boolean hasMonthly = checkAlertStates.get("MonthlyAlert");
        boolean hasQuarterly = checkAlertStates.get("QuarterlyAlert");
        boolean hasYearly = checkAlertStates.get("YearlyAlert");
        boolean hasGeneral = checkAlertStates.get("GeneralAlert");

        // Controllo combinato di tutti gli alert specifici
        if (hasMonthly && hasQuarterly && hasYearly) {
            return Response.ok(futureType.equals("All") ? "ok" : "All").build();
        }
        if (hasMonthly) {
            return Response.ok(futureType.equals("Monthly") ? "ok" : "Monthly").build();
        }
        if (hasQuarterly) {
            return Response.ok(futureType.equals("Quarterly") ? "ok" : "Quarterly").build();
        }
        if (hasYearly) {
            return Response.ok(futureType.equals("Yearly") ? "ok" : "Yearly").build();
        }
        if (hasGeneral) {
            return Response.ok(futureType.equals("General") ? "ok" : "General").build();
        }

        return Response.ok("Nessun alert attivo").build();
    }

    // Invia un'email al cliente
    @Path("/send-email")
    @POST
    @Consumes(MediaType.APPLICATION_JSON) // Consumi JSON
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendEmailToCliente(@CookieParam("SESSION_COOKIE") int sessionId, JsonObject params) {
        try {
            int idUtente = sessionService.trovaUtentebBySessione(sessionId);

            if (idUtente == 0) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida")
                        .build();
            }

            Cliente cliente = clienteService.getCliente(idUtente);
            if (cliente == null || cliente.getEmail() == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Cliente o email non trovati. cliente " + cliente.getUsername())
                        .build();
            }
            System.out.println(params);
            // Estrai la variabile deleteAlert dal corpo della richiesta
            JsonObject deleteAlert = params.getJsonObject("deleteAlert");
            // Accedi ai valori della variabile deleteAlert
            boolean isActive = deleteAlert.getBoolean("active", false);
            String message = deleteAlert.getString("message", "");
            Map<String, Boolean> checkDeleteAlert = null;
            System.out.println(isActive + " " + message);
            if (isActive) {
                // Elimina gli alert dell'utente
                checkDeleteAlert = clienteService.deleteUserAlert(idUtente, message);

                // Controlla se l'eliminazione è andata a buon fine
                if (checkDeleteAlert == null || !checkDeleteAlert.containsValue(true)) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Errore nell'eliminazione dell'alert.")
                            .build(); // Interrompe l'esecuzione se l'eliminazione fallisce
                }
            }
            System.out.println("Alert eliminato");


            String futuresType = params.getString("futuresType");
            boolean checkEmail = params.getBoolean("activeAlert");
            String corpoEmail = "Ciao " + cliente.getUsername() + ",\n\n";
            System.out.println(corpoEmail);

            if (futuresType.equals("All")) {
                boolean[] checkModalities = new boolean[]{
                        params.getBoolean("checkModalityYearly"),
                        params.getBoolean("checkModalityQuarterly"),
                        params.getBoolean("checkModalityMonthly")
                };
                double[] minimumLevels = new double[]{
                        params.containsKey("minimumLevelYearly") ?
                                (params.get("minimumLevelYearly") instanceof JsonNumber ?
                                        params.getJsonNumber("minimumLevelYearly").doubleValue() :
                                        Double.parseDouble(params.getString("minimumLevelYearly"))) :
                                0.0,

                        params.containsKey("minimumLevelQuarterly") ?
                                (params.get("minimumLevelQuarterly") instanceof JsonNumber ?
                                        params.getJsonNumber("minimumLevelQuarterly").doubleValue() :
                                        Double.parseDouble(params.getString("minimumLevelQuarterly"))) :
                                0.0,

                        params.containsKey("minimumLevelMonthly") ?
                                (params.get("minimumLevelMonthly") instanceof JsonNumber ?
                                        params.getJsonNumber("minimumLevelMonthly").doubleValue() :
                                        Double.parseDouble(params.getString("minimumLevelMonthly"))) :
                                0.0
                };

                double[] maximumLevels = new double[]{
                        params.containsKey("maximumLevelYearly") ?
                                (params.get("maximumLevelYearly") instanceof JsonNumber ?
                                        params.getJsonNumber("maximumLevelYearly").doubleValue() :
                                        Double.parseDouble(params.getString("maximumLevelYearly"))) :
                                0.0,

                        params.containsKey("maximumLevelQuarterly") ?
                                (params.get("maximumLevelQuarterly") instanceof JsonNumber ?
                                        params.getJsonNumber("maximumLevelQuarterly").doubleValue() :
                                        Double.parseDouble(params.getString("maximumLevelQuarterly"))) :
                                0.0,

                        params.containsKey("maximumLevelMonthly") ?
                                (params.get("maximumLevelMonthly") instanceof JsonNumber ?
                                        params.getJsonNumber("maximumLevelMonthly").doubleValue() :
                                        Double.parseDouble(params.getString("maximumLevelMonthly"))) :
                                0.0
                };

                String[] frequencies = new String[]{
                        params.getString("frequencyYearly"),
                        params.getString("frequencyQuarterly"),
                        params.getString("frequencyMonthly")
                };
                String[] futuresTypes = new String[]{
                        params.getString("futuresYearly"),
                        params.getString("futuresQuarterly"),
                        params.getString("futuresMonthly")
                };

                for (int i = 0; i < futuresTypes.length; i++) {
                    corpoEmail += "Futures Type: " + futuresTypes[i] + "\n";
                    corpoEmail += "Livello minimo: " + minimumLevels[i] + (checkModalities[i] ? "%" : "") + "\n";
                    corpoEmail += "Livello massimo: " + maximumLevels[i] + (checkModalities[i] ? "%" : "") + "\n";
                    corpoEmail += "Frequenza: " + frequencies[i] + "\n\n";
                }
                clienteService.updateDataFuturesAlert(idUtente,futuresType, maximumLevels, minimumLevels,frequencies,checkModalities, checkEmail);

            } else {
                double[] minimumLevel = new double[]{
                        params.containsKey("minimumLevel") ?
                                (params.get("minimumLevel") instanceof JsonNumber ?
                                        params.getJsonNumber("minimumLevel").doubleValue() :
                                        Double.parseDouble(params.getString("minimumLevel"))) :
                                0.0
                };

                double[] maximumLevel = new double[]{
                        params.containsKey("maximumLevel") ?
                                (params.get("maximumLevel") instanceof JsonNumber ?
                                        params.getJsonNumber("maximumLevel").doubleValue() :
                                        Double.parseDouble(params.getString("maximumLevel"))) :
                                0.0
                };
                boolean[] checkModality = new boolean[]{
                        params.getBoolean("checkModality")
                };
                String[] frequency = new String[]{
                        params.getString("frequencyAlert")
                };

                corpoEmail += "Le tue informazioni sono state aggiornate:\n";
                corpoEmail += "Futures Type: " + futuresType + "\n";
                corpoEmail += "Livello minimo: " + minimumLevel[0] + (checkModality[0] ? "%" : "") + "\n";
                corpoEmail += "Livello massimo: " + maximumLevel[0] + (checkModality[0] ? "%" : "") + "\n";
                corpoEmail += "Frequenza: " + frequency[0] + "\n";
                System.out.println(idUtente+ " " +futuresType+ " " + maximumLevel[0]+ " " + minimumLevel[0]+ " " +frequency[0]+ " " +checkModality[0]);
                clienteService.updateDataFuturesAlert(idUtente,futuresType, maximumLevel, minimumLevel,frequency,checkModality, checkEmail);

            }
            System.out.println(corpoEmail + " " + cliente.getEmail());
            // Corpo dell'email aggiornato
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
                    .entity("Errore durante l'invio dell'email.")
                    .build();

        }
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

            AlertData[] alertData = clienteService.checkUserAlertFillField(idUtente);
            Boolean checkEmail = clienteService.getCheckEmailStatus(idUtente);

            // Se alertData è null, restituisci un array vuoto per evitare NullPointerException
            if (alertData == null) {
                alertData = new AlertData[0];
            }

            AlertResponse response = new AlertResponse(alertData, checkEmail != null ? checkEmail : false);
            System.out.println(response.toString());
            return Response.ok(response).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while checking alerts.")
                    .build();
        }
    }

    @Path("/sendWeeklyEmail")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendWeeklyEmail1() {
        emailScheduler.sendScheduledEmail();
        return null;
    }

    @Path("/send-weekly-email")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendWeeklyEmail() {
        try {
            String recipient = "fiorenicolo.c@gmail.com";
            String subject = "Riepilogo Settimanale Mercato Energetico";

            // Simuliamo il recupero dati (in futuro potresti passarlo come parametro o prelevarlo dal DB)
            Map<String, Double> settlementStart = Map.of(
                    "2/25", 145.32, "3/25", 156.20, "4/25", 147.09,
                    "1/26", 140.25, "2/26", 119.68, "3/26", 123.33, "4/26", 123.14
            );
            Map<String, Double> settlementEnd = Map.of(
                    "2/25", 130.05, "3/25", 143.04, "4/25", 138.23,
                    "1/26", 133.65, "2/26", 107.42, "3/26", 113.67, "4/26", 113.60
            );

            // Costruiamo il corpo dell'email dinamicamente
            String body = buildEmailBody(settlementStart, settlementEnd);

            Mail mail = Mail.withHtml(recipient, subject, body);
            mailer.send(mail);

            return Response.ok("Email inviata con successo a " + recipient).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nell'invio dell'email: " + e.getMessage()).build();
        }
    }

    // Metodo per costruire l'email dinamicamente
    private String buildEmailBody(Map<String, Double> start, Map<String, Double> end) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ciao User1,\n\n")
                .append("Questa settimana il mercato energetico ha avuto queste variazioni:\n\n")
                .append("Dal giorno 10 febbraio 2025 al 14 febbraio 2025:\n\n");

        sb.append("<b>Quarter inizio settimana</b>\n<table border='1'><tr><th>Anno/Mese</th><th>Settlement Price</th></tr>");
        for (var entry : start.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table>\n\n");

        sb.append("<b>Quarter fine settimana</b>\n<table border='1'><tr><th>Anno/Mese</th><th>Settlement Price</th></tr>");
        for (var entry : end.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table>\n\n");

        sb.append("<b>Variazioni Percentuali</b>\n<table border='1'><tr><th>Anno/Mese</th><th>Variazione %</th></tr>");
        for (var entry : start.entrySet()) {
            String key = entry.getKey();
            double variation = ((end.get(key) - entry.getValue()) / entry.getValue()) * 100;
            sb.append("<tr><td>").append(key).append("</td><td>").append(String.format("%.1f", variation)).append("%</td></tr>");
        }
        sb.append("</table>\n\n");

        return sb.toString();
    }

}
