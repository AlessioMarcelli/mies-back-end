package miesgroup.mies.webdev.Service;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import miesgroup.mies.webdev.Model.*;
import miesgroup.mies.webdev.Repository.*;

import java.util.*;

@ApplicationScoped
public class EmailService {

    @Inject ClienteRepo clienteRepo;
    @Inject PodRepo podRepo;
    @Inject GeneralAlertRepo generalAlertRepo;
    @Inject MonthlyAlertRepo monthlyAlertRepo;
    @Inject QuarterlyAlertRepo quarterlyAlertRepo;
    @Inject YearlyAlertRepo yearlyAlertRepo;

    public boolean toggleEmailStatus(int idUtente, boolean frontendValue) {
        Cliente cliente = clienteRepo.findById(idUtente);
        if (cliente == null) return false;
        Boolean current = cliente.getCheckEmail();
        cliente.setCheckEmail(current == null ? frontendValue : !current);
        return true;
    }

    public Boolean getCheckEmailStatus(int idUtente) {
        Cliente cliente = clienteRepo.findById(idUtente);
        return cliente != null ? cliente.getCheckEmail() : null;
    }

    public Map<String, Boolean> checkUserAlert(int idUtente, String futuresType) {
        Map<String, Boolean> result = new HashMap<>();
        switch (futuresType) {
            case "General" -> result.put("GeneralAlert", generalAlertRepo.existsByUserId(idUtente));
            case "Monthly" -> result.put("MonthlyAlert", monthlyAlertRepo.existsByUserId(idUtente));
            case "Quarterly" -> result.put("QuarterlyAlert", quarterlyAlertRepo.existsByUserId(idUtente));
            case "Yearly" -> result.put("YearlyAlert", yearlyAlertRepo.existsByUserId(idUtente));
            case "All" -> {
                result.put("GeneralAlert", generalAlertRepo.existsByUserId(idUtente));
                result.put("MonthlyAlert", monthlyAlertRepo.existsByUserId(idUtente));
                result.put("QuarterlyAlert", quarterlyAlertRepo.existsByUserId(idUtente));
                result.put("YearlyAlert", yearlyAlertRepo.existsByUserId(idUtente));
            }
        }
        return result;
    }

    public Map<String, Boolean> deleteUserAlert(int idUtente, String futuresType) {
        Map<String, Boolean> result = new HashMap<>();
        switch (futuresType) {
            case "General" -> result.put("GeneralAlert", generalAlertRepo.deleteByUserId(idUtente));
            case "Monthly" -> result.put("MonthlyAlert", monthlyAlertRepo.deleteByUserId(idUtente));
            case "Quarterly" -> result.put("QuarterlyAlert", quarterlyAlertRepo.deleteByUserId(idUtente));
            case "Yearly" -> result.put("YearlyAlert", yearlyAlertRepo.deleteByUserId(idUtente));
            case "All" -> {
                result.put("GeneralAlert", generalAlertRepo.deleteByUserId(idUtente));
                result.put("MonthlyAlert", monthlyAlertRepo.deleteByUserId(idUtente));
                result.put("QuarterlyAlert", quarterlyAlertRepo.deleteByUserId(idUtente));
                result.put("YearlyAlert", yearlyAlertRepo.deleteByUserId(idUtente));
            }
        }
        return result;
    }

    public boolean updateDataFuturesAlert(int idUtente, String futuresType, double[] maxPriceValue, double[] minPriceValue, String[] frequency, boolean[] checkModality, boolean checkEmail) {
        Cliente cliente = clienteRepo.findById(idUtente);
        if (cliente == null) return false;

        boolean updated = false;

        if ("All".equals(futuresType)) {
            updated |= generalAlertRepo.saveOrUpdate(cliente, maxPriceValue[3], minPriceValue[3], frequency[3], checkModality[3]);
            updated |= monthlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[2], minPriceValue[2], frequency[2], checkModality[2]);
            updated |= quarterlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[1], minPriceValue[1], frequency[1], checkModality[1]);
            updated |= yearlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[0], minPriceValue[0], frequency[0], checkModality[0]);
        } else {
            int index = switch (futuresType) {
                case "General" -> 3;
                case "Monthly" -> 2;
                case "Quarterly" -> 1;
                case "Yearly" -> 0;
                default -> throw new IllegalArgumentException("Tipo non valido");
            };
            updated |= switch (futuresType) {
                case "General" -> generalAlertRepo.saveOrUpdate(cliente, maxPriceValue[index], minPriceValue[index], frequency[index], checkModality[index]);
                case "Monthly" -> monthlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[index], minPriceValue[index], frequency[index], checkModality[index]);
                case "Quarterly" -> quarterlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[index], minPriceValue[index], frequency[index], checkModality[index]);
                case "Yearly" -> yearlyAlertRepo.saveOrUpdate(cliente, maxPriceValue[index], minPriceValue[index], frequency[index], checkModality[index]);
                default -> false;
            };
        }

        toggleEmailStatus(idUtente, checkEmail);
        return updated;
    }

    public Map<String, Boolean> checkAlertStates(int idUtente) {
        return checkUserAlert(idUtente, "All");
    }



    public List<Map<String, Object>> checkUserAlertFillField(int idUtente) {
        List<Map<String, Object>> alerts = new ArrayList<>();

        generalAlertRepo.findByUserId(idUtente).ifPresent(g -> {
            Map<String, Object> map = new HashMap<>();
            map.put("futuresType", "GeneralAlert");
            map.put("maxPriceValue", g.getMaxPriceValue());
            map.put("minPriceValue", g.getMinPriceValue());
            map.put("frequencyA", g.getFrequencyA());
            map.put("checkModality", g.getCheckModality());
            map.put("idUtente", idUtente);
            alerts.add(map);
        });

        monthlyAlertRepo.findByUserId(idUtente).ifPresent(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("futuresType", "MonthlyAlert");
            map.put("maxPriceValue", m.getMaxPriceValue());
            map.put("minPriceValue", m.getMinPriceValue());
            map.put("frequencyA", m.getFrequencyA());
            map.put("checkModality", m.getCheckModality());
            map.put("idUtente", idUtente);
            alerts.add(map);
        });

        quarterlyAlertRepo.findByUserId(idUtente).ifPresent(q -> {
            Map<String, Object> map = new HashMap<>();
            map.put("futuresType", "QuarterlyAlert");
            map.put("maxPriceValue", q.getMaxPriceValue());
            map.put("minPriceValue", q.getMinPriceValue());
            map.put("frequencyA", q.getFrequencyA());
            map.put("checkModality", q.getCheckModality());
            map.put("idUtente", idUtente);
            alerts.add(map);
        });

        yearlyAlertRepo.findByUserId(idUtente).ifPresent(y -> {
            Map<String, Object> map = new HashMap<>();
            map.put("futuresType", "YearlyAlert");
            map.put("maxPriceValue", y.getMaxPriceValue());
            map.put("minPriceValue", y.getMinPriceValue());
            map.put("frequencyA", y.getFrequencyA());
            map.put("checkModality", y.getCheckModality());
            map.put("idUtente", idUtente);
            alerts.add(map);
        });

        return alerts;
    }

}
