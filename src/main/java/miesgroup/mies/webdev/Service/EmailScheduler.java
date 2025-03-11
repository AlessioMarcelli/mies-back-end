package miesgroup.mies.webdev.Service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.AlertData;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.Future;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class EmailScheduler {
    /*TODO
     * - se futures type All bisogna presentare all'utente tutte e tre le tabelle in riga (se triggherato da alert)
     * - correggo presa delle date da mese a mese
     * - abbellisco Email styling
     * - finisco firma email
     * - dopo l'estrazione future faccio controllo con alert utente se viene triggherato allora l'email verrà inviata
     */

    private final Mailer mailer;

    @Inject
    ClienteService clienteService;

    @Inject
    FuturesService futuresService;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    public EmailScheduler(Mailer mailer) {
        this.mailer = mailer;
    }

    //secondi (0-59), minuti (0-59), ore (0-23), giorno del mese (1-31), mese (1-12, JAN-DEC), giorno della settimana 1-7 (o SUN-SAT)
    //se ? = qualsiasi o * = nessuno
    //@Scheduled(cron = "0 19 18 ? * *") //test
    //@Scheduled(cron = "0 0 8 ? * MON") // Ogni lunedì alle 08:00
    public void sendScheduledEmail() {
        LocalDate today = LocalDate.now();
        LocalDate firstMonday = today.with(TemporalAdjusters.firstInMonth(java.time.DayOfWeek.MONDAY));

        List<Cliente> clients = clienteService.getClientsCheckEmail();

        for (Cliente cliente : clients) {
            System.out.println("Verifica alert per cliente ID: " + cliente.getId());

            AlertData[] alertData = clienteService.checkUserAlertFillField(cliente.getId());
            sendRegularEmail(cliente, alertData);
            if (today.equals(firstMonday)) {
                //sendSpecialEmail(cliente, alertData);
            } else {

            }
        }
    }

    private void sendRegularEmail(Cliente cliente, AlertData[] alertData) {
        LocalDate previousMonday = DateUtils.getPreviousMonday(LocalDate.now());
        LocalDate previousFriday = DateUtils.getPreviousFriday(LocalDate.now());
        String previousMondayStr = previousMonday.toString();
        String previousFridayStr = previousFriday.toString();
        List<Future> futures = new ArrayList<>();

        if (alertData != null) {
            List<Future> extractedFutures;

            if (alertData.length == 3) {
                extractedFutures = futuresService.getFuturesYear(previousMondayStr);
                futures.addAll(extractedFutures);
                extractedFutures = futuresService.getFuturesYear(previousFridayStr);
                futures.addAll(extractedFutures);
                extractedFutures = futuresService.getFuturesQuarter(previousMondayStr);
                futures.addAll(extractedFutures);
                extractedFutures = futuresService.getFuturesQuarter(previousFridayStr);
                futures.addAll(extractedFutures);
                extractedFutures = futuresService.getFuturesMonth(previousMondayStr);
                futures.addAll(extractedFutures);
                extractedFutures = futuresService.getFuturesMonth(previousFridayStr);
                futures.addAll(extractedFutures);
            } else {
                String type = alertData[0].getFuturesType();
                if ("MonthlyAlert".equals(type)) {
                    extractedFutures = futuresService.getFuturesMonth(previousMondayStr);
                    futures.addAll(extractedFutures);
                    extractedFutures = futuresService.getFuturesMonth(previousFridayStr);
                    futures.addAll(extractedFutures);
                } else if ("QuarterlyAlert".equals(type)) {
                    extractedFutures = futuresService.getFuturesQuarter(previousMondayStr);
                    futures.addAll(extractedFutures);
                    extractedFutures = futuresService.getFuturesQuarter(previousFridayStr);
                    futures.addAll(extractedFutures);
                } else if ("YearlyAlert".equals(type)) {
                    extractedFutures = futuresService.getFuturesYear(previousMondayStr);
                    futures.addAll(extractedFutures);
                    extractedFutures = futuresService.getFuturesYear(previousFridayStr);
                    futures.addAll(extractedFutures);
                }
            }
        }

        // Creazione mappa prezzi per inizio e fine settimana
        Map<String, Double> settlementStart = new HashMap<>();
        Map<String, Double> settlementEnd = new HashMap<>();

        for (Future future : futures) {
            if (future.getDate() != null) {  // Evita il NullPointerException
                if (future.getDate().equals(previousMondayStr)) {
                    settlementStart.put("future.getFutureType()", future.getSettlementPrice());
                } else if (future.getDate().equals(previousFridayStr)) {
                    settlementEnd.put("future.getFutureType()", future.getSettlementPrice());
                }
            } else {
                System.out.println("Attenzione: un oggetto Future ha la data null! " + future);
            }
        }

        // Creazione corpo email
        String body = buildEmailBody(settlementStart, settlementEnd, previousMondayStr, previousFridayStr, cliente);

        mailer.send(
                Mail.withHtml(cliente.getEmail(), "Email Settimanale", body)
        );
        System.out.println("Email settimanale inviata a: " + cliente.getEmail());
    }

    // Metodo per costruire il corpo dell'email
    private String buildEmailBody(Map<String, Double> start, Map<String, Double> end, String startDate, String endDate, Cliente cliente) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ciao ").append(cliente.getUsername()).append(",\n\n")
                .append("Ecco il riepilogo dei futures per il periodo " + startDate + " - " + endDate + ":\n\n");

        sb.append("<b>Prezzi di Inizio Settimana</b><table border='1'><tr><th>Futures</th><th>Prezzo</th></tr>");
        for (var entry : start.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table><br>");

        sb.append("<b>Prezzi di Fine Settimana</b><table border='1'><tr><th>Futures</th><th>Prezzo</th></tr>");
        for (var entry : end.entrySet()) {
            sb.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(entry.getValue()).append("</td></tr>");
        }
        sb.append("</table><br>");

        sb.append("<b>Variazione Percentuale</b><table border='1'><tr><th>Futures</th><th>Variazione %</th></tr>");
        for (var entry : start.entrySet()) {
            String key = entry.getKey();
            if (end.containsKey(key)) {
                double variation = ((end.get(key) - entry.getValue()) / entry.getValue()) * 100;
                sb.append("<tr><td>").append(key).append("</td><td>").append(String.format("%.1f", variation)).append("%</td></tr>");
            }
        }
        sb.append("</table><br>");

        sb.append("Buona giornata!\n");
        return sb.toString();
    }


    private void sendSpecialEmail(Cliente cliente, AlertData[] alertData) {
        //String emailContent = generateSpecialEmailContent(alertData);
        LocalDate today = LocalDate.now();
        LocalDate firstBusinessDay = DateUtils.getFirstBusinessDayOfPreviousMonth(today);
        LocalDate lastBusinessDay = DateUtils.getLastBusinessDayOfPreviousMonth(today);

        System.out.println("Dati da usare: " + firstBusinessDay + " -> " + lastBusinessDay);

        mailer.send(
                Mail.withText(cliente.getEmail(),
                        "Email Speciale del Primo Lunedì",
                        "ciao"
                )
        );
        System.out.println("Email speciale inviata a: " + cliente.getEmail());
    }
/*
    private String generateEmailContent(AlertData[] alertData) {

        StringBuilder sb = new StringBuilder("Questa è l'email settimanale.\n\nAlert ricevuti:\n");

        for (AlertData alert : alertData) {
            sb.append("- ").append(alert.getMessage()).append("\n");
        }

        return sb.toString();
    }

    private String generateSpecialEmailContent(AlertData[] alertData) {
        StringBuilder sb = new StringBuilder("Questa è l'email speciale del primo lunedì del mese.\n\nAlert ricevuti:\n");

        for (AlertData alert : alertData) {
            sb.append("- ").append(alert.getMessage()).append("\n");
        }

        return sb.toString();
    }

 */
}