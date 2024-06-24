package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Bolletta;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class BollettaRepo {

    private final DataSource dataSource;

    public BollettaRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(List<Double> dati) throws SQLException {
    }
}
