package miesgroup.mies.webdev.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.GeneralAlert;
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

    public boolean saveOrUpdate(Cliente cliente, double max, double min, boolean check) {
        Optional<MonthlyAlert> existing = find("idUtente", cliente.getId()).firstResultOptional();

        MonthlyAlert alert = existing.orElseGet(MonthlyAlert::new);

        alert.setIdUtente(cliente.getId());
        alert.setUtente(cliente);
        alert.setMaxPriceValue(max);
        alert.setMinPriceValue(min);
        alert.setCheckModality(check);

        if (existing.isEmpty()) {
            persist(alert); // solo se nuovo
        }

        return true;
    }
}
