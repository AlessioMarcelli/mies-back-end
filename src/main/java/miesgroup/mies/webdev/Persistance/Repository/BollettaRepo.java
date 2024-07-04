package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Bolletta;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApplicationScoped
public class BollettaRepo {

    private final DataSource dataSource;

    public BollettaRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void insert(Bolletta bolletta) throws SQLException {
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

}
