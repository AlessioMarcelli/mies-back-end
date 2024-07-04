package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Costi;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
public class CostiRepo {
    private final DataSource dataSource;

    public CostiRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void aggiungiCostoTrimestrale(Costi costo) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO costi (descrizione, categoria, unità_misura, trimestrale, costo) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);) {
                statement.setString(1, costo.getDescrizione());
                statement.setString(2, costo.getCategoria());
                statement.setString(3, costo.getUnitaMisura());
                statement.setInt(4, costo.getTrimestre());
                statement.setDouble(5, costo.getCosto());
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void aggiungiCostoAnnuale(Costi costo) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO costi (descrizione, categoria, unità_misura, annuale, costo) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);) {
                statement.setString(1, costo.getDescrizione());
                statement.setString(2, costo.getCategoria());
                statement.setString(3, costo.getUnitaMisura());
                statement.setString(4, costo.getAnno());
                statement.setDouble(5, costo.getCosto());
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Costi> getAllCosti() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Descrizione, Unità_Misura, Trimestre, Annuale, Costo, Categoria FROM dettaglio_costo");) {
                try (ResultSet resultSet = statement.executeQuery();) {
                    ArrayList<Costi> costi = new ArrayList<>();
                    while (resultSet.next()) {
                        Costi costo = new Costi();
                        costo.setDescrizione(resultSet.getString("Descrizione"));
                        costo.setCategoria(resultSet.getString("Categoria"));
                        costo.setUnitaMisura(resultSet.getString("Unità_Misura"));
                        costo.setTrimestre(resultSet.getInt("Trimestrale"));
                        costo.setAnno(resultSet.getString("Annuale"));
                        costo.setCosto(resultSet.getDouble("Costo"));
                        costi.add(costo);
                    }
                    return costi;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
