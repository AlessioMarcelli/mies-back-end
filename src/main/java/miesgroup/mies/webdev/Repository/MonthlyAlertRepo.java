package miesgroup.mies.webdev.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.MonthlyAlert;
import miesgroup.mies.webdev.Model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Optional;

@ApplicationScoped
public class MonthlyAlertRepo implements PanacheRepositoryBase<MonthlyAlert, Long> {

    public boolean existsByUserId(int userId) {
        return count("utente.id", userId) > 0;
    }

    public boolean deleteByUserId(int userId) {
        return delete("utente.id", userId) > 0;
    }

    public Optional<MonthlyAlert> findByUserId(int userId) {
        return find("utente.id", userId).firstResultOptional();
    }

    public boolean saveOrUpdate(Cliente cliente, double max, double min, String frequency, boolean check) {
        Optional<MonthlyAlert> existing = findByUserId(cliente.getId());
        MonthlyAlert alert = existing.orElseGet(MonthlyAlert::new);
        alert.setUtente(cliente);
        alert.setMaxPriceValue(max);
        alert.setMinPriceValue(min);
        alert.setFrequencyA(frequency);
        alert.setCheckModality(check);
        alert.persist();
        return true;
    }
}
