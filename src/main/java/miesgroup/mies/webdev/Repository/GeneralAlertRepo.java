package miesgroup.mies.webdev.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.GeneralAlert;
import miesgroup.mies.webdev.Model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Optional;

@ApplicationScoped
public class GeneralAlertRepo implements PanacheRepositoryBase<GeneralAlert, Long> {

    public boolean existsByUserId(int userId) {
        return count("utente.id", userId) > 0;
    }

    public boolean deleteByUserId(int userId) {
        return delete("utente.id", userId) > 0;
    }

    public Optional<GeneralAlert> findByUserId(int userId) {
        return find("utente.id", userId).firstResultOptional();
    }

    public boolean saveOrUpdate(Cliente cliente, double max, double min, String frequency, boolean check) {
        Optional<GeneralAlert> existing = findByUserId(cliente.getId());
        GeneralAlert alert = existing.orElseGet(GeneralAlert::new);
        alert.setUtente(cliente);
        alert.setMaxPriceValue(max);
        alert.setMinPriceValue(min);
        alert.setFrequencyA(frequency);
        alert.setCheckModality(check);
        alert.persist();
        return true;
    }
}