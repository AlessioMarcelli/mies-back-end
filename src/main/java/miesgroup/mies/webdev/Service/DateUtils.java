package miesgroup.mies.webdev.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    // Metodo per ottenere il mese come stringa
    public static String getMonthFromDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("La data non può essere null");
        }
        // Converte Date in LocalDate
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        // Restituisce il mese come stringa
        return localDate.getMonth().toString(); // Esempio: "JANUARY"
    }

    public static String getMonthFromDateLocalized(Date date) {
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN); // Esempio: "gennaio"
    }

    // Metodo per ottenere il mese come numero (1-12)
    public static int getMonthNumberFromDate(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("La data non può essere null");
        }
        // Converte Date in LocalDate
        LocalDate localDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        // Restituisce il mese come numero
        return localDate.getMonthValue(); // Esempio: 1 (Gennaio)
    }
}
