package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Bolletta;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class BollettaRepo {

    private final DataSource dataSource;

    public BollettaRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void A2Ainsert(Bolletta bolletta) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO bolletta_pod (F1_Attiva,F2_Attiva,F3_Attiva,F1_Reattiva,F2_Reattiva,F3_Reattiva,F1_Potenza,F2_Potenza,F3_Potenza,Spese_Energia,Oneri,Imposte,Spese_Trasporto, Nome_Bolletta, Periodo_Inizio, Periodo_Fine,id_pod) VALUES (?, ? , ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ", PreparedStatement.RETURN_GENERATED_KEYS
            )) {
                statement.setDouble(1, bolletta.getF1A());
                statement.setDouble(2, bolletta.getF2A());
                statement.setDouble(3, bolletta.getF3A());
                statement.setDouble(4, bolletta.getF1R());
                statement.setDouble(5, bolletta.getF2R());
                statement.setDouble(6, bolletta.getF3R());
                statement.setDouble(7, bolletta.getF1P());
                statement.setDouble(8, bolletta.getF2P());
                statement.setDouble(9, bolletta.getF3P());
                statement.setDouble(10, bolletta.getSpese_Energia());
                statement.setDouble(11, bolletta.getOneri());
                statement.setDouble(12, bolletta.getImposte());
                statement.setDouble(13, bolletta.getTrasporti());
                statement.setString(14, bolletta.getNomeBolletta());
                statement.setDate(15, bolletta.getPeriodoInizio());
                statement.setDate(16, bolletta.getPeriodoFine());
                statement.setString(17, bolletta.getId_pod());
                statement.executeUpdate();
                try (var generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        bolletta.setId(id);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error inserting bolletta into database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public Double getCorrispettiviDispacciamentoA2A(int trimestre) throws SQLException {
        String query = "SELECT SUM(Costo) FROM dettaglio_costo WHERE Unità_Misura = '€/KWh' AND Categoria = 'dispacciamento' AND (Trimestrale = ? OR Annuale IS NOT NULL)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Imposta il valore del parametro
            statement.setInt(1, trimestre);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getDouble(1); // Ottieni il risultato della somma
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting consumption from database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }

        return null; // Se non ci sono risultati, restituisce null
    }


    public Double getConsumoA2A(String nome) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT SUM(F1_Attiva+F2_Attiva+F3_Attiva) FROM bolletta_pod WHERE Nome_Bolletta = ?")) {
                statement.setString(1, nome);
                statement.executeQuery();
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getDouble(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting consumption from database", e);
            }

        }
        return null;
    }

    public String getTipoTensione(String idPod) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Tipo_Tensione FROM pod WHERE Id_Pod = ?")) {
                statement.setString(1, idPod);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting tensione from database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
        return null;
    }

    public void updateDispacciamentoA2A(double dispacciamento, String nomeBolletta) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE bolletta_pod SET Dispacciamento = ? WHERE Nome_Bolletta = ?")) {
                statement.setDouble(1, dispacciamento);
                statement.setString(2, nomeBolletta);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating dispacciamento in database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public void updateGenerationA2A(Double generation, String nomeBolletta) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE bolletta_pod SET Generation = ? WHERE Nome_Bolletta = ?")) {
                statement.setDouble(1, generation);
                statement.setString(2, nomeBolletta);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating generation in database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public void updateMeseBolletta(String mese, String nomeBolletta) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("UPDATE bolletta_pod SET Mese = ? WHERE Nome_Bolletta = ?")) {
                statement.setString(1, mese);
                statement.setString(2, nomeBolletta);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating mese in database", e);
            }
        }
    }

    public String getMese(String nomeBolletta) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Mese FROM bolletta_pod WHERE Nome_Bolletta = ?")) {
                statement.setString(1, nomeBolletta);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error getting mese from database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
        return null;
    }
}