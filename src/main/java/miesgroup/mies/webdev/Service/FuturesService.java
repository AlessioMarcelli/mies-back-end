package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.Future;
import miesgroup.mies.webdev.Repository.FuturesRepo;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class FuturesService {
    private final FuturesRepo futuresRepo;

    @Inject
    public FuturesService(FuturesRepo futuresRepo) {
        this.futuresRepo = futuresRepo;
    }

    // Metodo per ottenere i futures in base alla data e al tipo di future (anno, trimestre, mese)
    public List<Future> getFutures(String date, String type) {
        try {
            switch (type) {
                case "year":
                    return futuresRepo.findByYear(date); // Query per ottenere tutti i futures annuali
                case "quarter":
                    return futuresRepo.findByQuarter(date); // Query per ottenere i futures trimestrali
                case "month":
                    return futuresRepo.findByMonth(date); // Query per ottenere i futures mensili
                default:
                    return List.of();  // Se il tipo non è valido, restituisci una lista vuota
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Future> getFuturesYear(String date) {
        try {
            return futuresRepo.findByYear(date); // Query per ottenere tutti i futures annuali
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Future> getFutureYear(String date) {
        try {
            return futuresRepo.findByYear(date); // Query per ottenere tutti i futures annuali
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Future> getFuturesQuarter(String date) {
        try {
            return futuresRepo.findByQuarter(date); // Query per ottenere tutti i futures annuali
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Future> getFuturesMonth(String date) {
        try {
            return futuresRepo.findByMonth(date); // Query per ottenere tutti i futures annuali
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }




    // Metodo per ottenere l'ultima data disponibile
    public String getLastDate() throws SQLException {
        return futuresRepo.getLastDateFromYearlyFutures(); // Chiamata al metodo nel repository per ottenere l'ultima data
    }

    // Metodo per determinare il trimestre in base al mese
    private String getQuarter(String month) {
        switch (month) {
            case "01":
            case "02":
            case "03":
                return "Q1";
            case "04":
            case "05":
            case "06":
                return "Q2";
            case "07":
            case "08":
            case "09":
                return "Q3";
            case "10":
            case "11":
            case "12":
                return "Q4";
            default:
                return "";
        }
    }
}