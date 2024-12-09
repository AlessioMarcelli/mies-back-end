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

    public void aggiungiCosto(Costi costo) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO dettaglio_costo (Descrizione, Categoria, Unità_Misura, Trimestrale, Costo, Intervallo_Potenza,Classe_Agevolazione) VALUES (?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);) {
                statement.setString(1, costo.getDescrizione());
                statement.setString(2, costo.getCategoria());
                statement.setString(3, costo.getUnitaMisura());
                statement.setInt(4, costo.getTrimestre());
                statement.setFloat(5, costo.getCosto());
                statement.setString(6, costo.getIntervalloPotenza());
                statement.setString(7, costo.getClasseAgevolazione());
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    costo.setId(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //from excele
    public void aggiungiCostoFromExcel(ArrayList<String> costi) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO dettaglio_costo (Descrizione, Unità_Misura, Trimestrale, Annuale, Costo, Categoria, Intervallo_Potenza, Classe_Agevolazione) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, costi.get(0)); // Descrizione
                statement.setString(2, costi.get(1)); // Unità_Misura
                statement.setInt(3, Math.round(Float.parseFloat(costi.get(2)))); // Trimestrale
                statement.setString(4, costi.get(3)); // Annuale
                statement.setFloat(5, Math.round(Float.parseFloat(costi.get(4)))); // Costo
                statement.setString(6, costi.get(5)); // Categoria
                statement.setString(7, costi.get(6)); // Intervallo_Potenza
                statement.setString(8, costi.get(7)); // Classe_Agevolazione

                statement.executeUpdate();

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        costi.add(String.valueOf(id)); // Aggiungi l'ID generato alla lista costi
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'inserimento dei costi nel database", e);
        }
    }

    public ArrayList<Costi> getAllCosti() {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Descrizione, Unità_Misura, Trimestrale, Annuale, Costo, Categoria,Id_Costo, Intervallo_Potenza, Classe_Agevolazione, Data_inserimento FROM dettaglio_costo");) {
                try (ResultSet resultSet = statement.executeQuery();) {
                    ArrayList<Costi> costi = new ArrayList<>();
                    while (resultSet.next()) {
                        Costi costo = new Costi();
                        costo.setId(resultSet.getInt("Id_Costo"));
                        costo.setDescrizione(resultSet.getString("Descrizione"));
                        costo.setCategoria(resultSet.getString("Categoria"));
                        costo.setUnitaMisura(resultSet.getString("Unità_Misura"));
                        costo.setTrimestre(resultSet.getInt("Trimestrale"));
                        costo.setAnno(resultSet.getString("Annuale"));
                        costo.setCosto(resultSet.getFloat("Costo"));
                        costo.setIntervalloPotenza(resultSet.getString("Intervallo_Potenza"));
                        costo.setClasseAgevolazione(resultSet.getString("Classe_Agevolazione"));
                        costo.setDataInserimento(resultSet.getDate("Data_inserimento"));
                        costi.add(costo);
                    }
                    return costi;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Costi getSum(String intervalloPotenza) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT SUM(Costo) AS Costo FROM dettaglio_costo WHERE Intervallo_Potenza = ? AND Categoria = 'trasporti' AND (Trimestrale = 2 OR Annuale IS NOT NULL) AND Unità_Misura = '€/KWh'");) {
                statement.setString(1, intervalloPotenza);
                try (ResultSet resultSet = statement.executeQuery();) {
                    if (resultSet.next()) {
                        Costi costo = new Costi();
                        costo.setCosto(resultSet.getFloat("Costo"));
                        return costo;
                    }
                    return null;
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void deleteCosto(int id) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM dettaglio_costo WHERE Id_Costo = ?");) {
                statement.setInt(1, id);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
