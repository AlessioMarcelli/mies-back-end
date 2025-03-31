package miesgroup.mies.webdev.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.Futures;

import java.time.LocalDate;

@ApplicationScoped
public class FuturesRepo implements PanacheRepository<Futures> {
    public Futures findLatest() {
        return find("ORDER BY date DESC").firstResult();
    }

    public boolean existsByDate(LocalDate date) {
        return count("date", date) > 0;
    }
}

