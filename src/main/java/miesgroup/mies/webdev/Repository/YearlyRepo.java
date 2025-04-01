package miesgroup.mies.webdev.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.Yearly;

@ApplicationScoped
public class YearlyRepo implements PanacheRepository<Yearly> {
}

