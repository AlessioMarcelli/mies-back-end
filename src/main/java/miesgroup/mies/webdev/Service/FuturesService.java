package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Repository.FuturesRepo;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class FuturesService {
    private final FuturesRepo futuresRepo;

    @Inject
    public FuturesService(FuturesRepo futuresRepo) {
        this.futuresRepo = futuresRepo;
    }

    // Metodo generico per ottenere i futures in base al tipo
    public List<Map<String, Object>> getFutures(String date, String type) {
        return switch (type) {
            case "year" -> getFuturesYear(date);
            case "quarter" -> getFuturesQuarter(date);
            case "month" -> getFuturesMonth(date);
            default -> List.of();  // Se il tipo non è valido, restituisci una lista vuota
        };
    }

    public List<Map<String, Object>> getFuturesYear(String date) {
        return futuresRepo.findByYear(date).stream()
                .map(y -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", y.getYear());
                    map.put("settlementPrice", y.getFuture().getSettlementPrice());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFuturesQuarter(String date) {
        return futuresRepo.findByQuarter(date).stream()
                .map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", q.getYear());
                    map.put("quarter", q.getQuarter());
                    map.put("settlementPrice", q.getFuture().getSettlementPrice());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFuturesMonth(String date) {
        return futuresRepo.findByMonth(date).stream()
                .map(m -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("year", m.getYear());
                    map.put("month", m.getMonth());
                    map.put("settlementPrice", m.getFuture().getSettlementPrice());
                    return map;
                })
                .collect(Collectors.toList());
    }

    public String getLastDate() {
        return futuresRepo.getLastDateFromYearlyFutures();
    }

    private String getQuarter(String month) {
        return switch (month) {
            case "01", "02", "03" -> "Q1";
            case "04", "05", "06" -> "Q2";
            case "07", "08", "09" -> "Q3";
            case "10", "11", "12" -> "Q4";
            default -> "";
        };
    }
}
