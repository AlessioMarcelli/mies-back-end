package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Fixing;

import java.util.List;

@ApplicationScoped
public class FixingRepo implements PanacheRepositoryBase<Fixing, Integer> {

    public List<Fixing> getFixing(Integer idC) {
        return find("utente.id", idC).list();
    }

}
