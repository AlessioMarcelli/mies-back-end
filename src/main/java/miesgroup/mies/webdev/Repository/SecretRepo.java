package miesgroup.mies.webdev.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.Secret;

@ApplicationScoped
public class SecretRepo implements PanacheRepositoryBase<Secret, Integer> {


}
