package miesgroup.mies.webdev.Service;

import io.quarkus.scheduler.Scheduled;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
/*TODO
 * - se futures type All bisogna presentare all'utente tutte e tre le tabelle in riga (se triggherato da alert)
 * - correggo presa delle date da mese a mese
 * - abbellisco Email styling
 * - finisco firma email
 * - dopo l'estrazione future faccio controllo con alert utente se viene triggherato allora l'email verrà inviata
 */
@ApplicationScoped
public class EmailScheduler {

    private final Mailer mailer;

    @Inject ClienteService clienteService;
    @Inject FuturesService futuresService;
    @Inject EmailService emailService;

    @ConfigProperty(name = "quarkus.mailer.from")
    String from;

    public EmailScheduler(Mailer mailer) {
        this.mailer = mailer;
    }
    // Ogni lunedì alle 8:00
    @Scheduled(cron = "0 0 8 ? * MON")
    public void sendWeeklyEmailEveryMonday() {
        sendScheduledEmail();
    }
    // Ogni 1 del mese alle 6:00
    @Scheduled(cron = "0 0 6 1 * ?")
    public void sendMonthlySpecialEmail() {
        LocalDate today = LocalDate.now();
        List<Cliente> clients = clienteService.getClientsCheckEmail();

        for (Cliente cliente : clients) {
            //List<PanacheEntityBase> alerts = emailService.checkUserAlertFillField(cliente.getId());
            //sendSpecialEmail(cliente, alerts);
        }
    }
    // Ogni giorno alle 5:00
    /*
    @Scheduled(cron = "0 0 5 * * ?")
    public void checkTriggeredAlertsDaily() {
        List<Cliente> clients = clienteService.listAll();

        for (Cliente cliente : clients) {
            List<PanacheEntityBase> alerts = emailService.checkUserAlertFillField(cliente.getId());
            if (!alerts.isEmpty()) {
                System.out.println("Alert trigger verificato per cliente ID: " + cliente.getId());
                // Logica personalizzata per gestire l'alert triggerato
            }
        }
    }
     */

    public void sendScheduledEmail() {
        LocalDate today = LocalDate.now();
        LocalDate firstMonday = today.with(TemporalAdjusters.firstInMonth(java.time.DayOfWeek.MONDAY));

        List<Cliente> clients = clienteService.getClientsCheckEmail();

        for (Cliente cliente : clients) {
            System.out.println("Verifica alert per cliente ID: " + cliente.getId());

            //List<PanacheEntityBase> alerts = emailService.checkUserAlertFillField(cliente.getId());
            //sendRegularEmail(cliente, alerts);

            if (today.equals(firstMonday)) {
                //sendSpecialEmail(cliente, alerts);
            }
        }
    }

    private void sendRegularEmail(Cliente cliente, List<PanacheEntityBase> alerts) {
        LocalDate previousMonday = DateUtils.getPreviousMonday(LocalDate.now());
        LocalDate previousFriday = DateUtils.getPreviousFriday(LocalDate.now());
        String previousMondayStr = previousMonday.toString();
        String previousFridayStr = previousFriday.toString();

        List<Map<String, Object>> futures = new ArrayList<>();

        for (PanacheEntityBase alert : alerts) {
            if (alert instanceof MonthlyAlert) {
                futures.addAll(futuresService.getFuturesMonth(previousMondayStr));
                futures.addAll(futuresService.getFuturesMonth(previousFridayStr));
            } else if (alert instanceof QuarterlyAlert) {
                futures.addAll(futuresService.getFuturesQuarter(previousMondayStr));
                futures.addAll(futuresService.getFuturesQuarter(previousFridayStr));
            } else if (alert instanceof YearlyAlert) {
                futures.addAll(futuresService.getFuturesYear(previousMondayStr));
                futures.addAll(futuresService.getFuturesYear(previousFridayStr));
            }
        }

        Map<String, Double> settlementStart = new HashMap<>();
        Map<String, Double> settlementEnd = new HashMap<>();

        for (Map<String, Object> future : futures) {
            Object dateObj = future.get("date");
            Object typeObj = future.get("future_type");
            Object priceObj = future.get("settlementPrice");

            if (dateObj == null || typeObj == null || priceObj == null) continue;

            String date = dateObj.toString();
            String futureType = typeObj.toString();
            double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : Double.parseDouble(priceObj.toString());

            if (date.equals(previousMondayStr)) {
                settlementStart.put(futureType, price);
            } else if (date.equals(previousFridayStr)) {
                settlementEnd.put(futureType, price);
            }
        }

        String body = buildEmailBody(settlementStart, settlementEnd, previousMondayStr, previousFridayStr, cliente);

        mailer.send(
                Mail.withHtml(cliente.getEmail(), "Email Settimanale", body)
        );
        System.out.println("Email settimanale inviata a: " + cliente.getEmail());
    }


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

    private void sendSpecialEmail(Cliente cliente, List<PanacheEntityBase> alerts) {
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
}
