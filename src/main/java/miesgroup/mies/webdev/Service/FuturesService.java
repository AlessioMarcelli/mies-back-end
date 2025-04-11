package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.Monthly;
import miesgroup.mies.webdev.Model.Quarterly;
import miesgroup.mies.webdev.Model.Yearly;
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
        switch (type) {
            case "year":
                return getFuturesYear(date);
            case "quarter":
                return getFuturesQuarter(date);
            case "month":
                return getFuturesMonth(date);
            default:
                return List.of();  // Se il tipo non è valido, restituisci una lista vuota
        }
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
