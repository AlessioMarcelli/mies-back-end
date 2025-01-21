package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Costi;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public boolean aggiungiCosto(Costi costo) throws SQLException {
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
                int rowsAffected = statement.executeUpdate();
                // Recupera la chiave generata
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            costo.setId(id); // Imposta l'ID nel modello
                            return true;
                        }
                    }
                }
                return false; // Nessuna riga inserita o nessuna chiave generata
            } catch (SQLException e) {
                // Log dell'errore (facoltativo, per debug)
                e.printStackTrace();
                throw new RuntimeException("Errore durante l'aggiunta del costo", e);
            }
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

    public boolean updateCosto(Costi c) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE dettaglio_costo SET Descrizione = ?, Categoria = ?, Unità_Misura = ?, Trimestrale = ?, Annuale = ?, Costo = ?, Intervallo_Potenza = ?, Classe_Agevolazione = ? WHERE Id_Costo = ?");) {
                statement.setString(1, c.getDescrizione());
                statement.setString(2, c.getCategoria());
                statement.setString(3, c.getUnitaMisura());
                statement.setInt(4, c.getTrimestre());
                statement.setString(5, c.getAnno());
                statement.setFloat(6, c.getCosto());
                statement.setString(7, c.getIntervalloPotenza());
                statement.setString(8, c.getClasseAgevolazione());
                statement.setInt(9, c.getId());
                int rowAffect = statement.executeUpdate();

                return rowAffect > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Costi dettaglioCosto) throws SQLException {
        String query = "INSERT INTO dettaglio_costo (Descrizione, Categoria, Unità_Misura, Trimestrale, Annuale, Costo, Intervallo_Potenza, Classe_Agevolazione) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, dettaglioCosto.getDescrizione());
            statement.setString(2, dettaglioCosto.getCategoria());
            statement.setString(3, dettaglioCosto.getUnitaMisura());
            statement.setInt(4, dettaglioCosto.getTrimestre());
            statement.setString(5, dettaglioCosto.getAnno());
            statement.setFloat(6, dettaglioCosto.getCosto());
            statement.setString(7, dettaglioCosto.getIntervalloPotenza());
            statement.setString(8, dettaglioCosto.getClasseAgevolazione());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        dettaglioCosto.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }
}
