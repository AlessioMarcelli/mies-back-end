package miesgroup.mies.webdev.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.YearlyAlert;
import miesgroup.mies.webdev.Model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Optional;

@ApplicationScoped
public class YearlyAlertRepo implements PanacheRepositoryBase<YearlyAlert, Long> {

    public boolean existsByUserId(int userId) {
        return count("utente.id", userId) > 0;
    }

    public boolean deleteByUserId(int userId) {
        return delete("utente.id", userId) > 0;
    }

    public Optional<YearlyAlert> findByUserId(int userId) {
        return find("utente.id", userId).firstResultOptional();
    }

    public boolean saveOrUpdate(Cliente cliente, double max, double min, String frequency, boolean check) {
        Optional<YearlyAlert> existing = findByUserId(cliente.getId());
        YearlyAlert alert = existing.orElseGet(YearlyAlert::new);
        alert.setUtente(cliente);
        alert.setMaxPriceValue(max);
        alert.setMinPriceValue(min);
        alert.setFrequencyA(frequency);
        alert.setCheckModality(check);
        alert.persist();
        return true;
    }
}
