package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.Sessione;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class SessionRepo {
    private final DataSource dataSources;

    public SessionRepo(DataSource dataSources) {
        this.dataSources = dataSources;
    }

    public int insertSession(int idUtente) throws SQLException {

        try (Connection connection = dataSources.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO sessione (id_utente) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, idUtente);
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return id;
                }
            }
        }
        throw new SQLException("Cannot insert new session for partecipante " + idUtente);
    }

    public Optional<Sessione> getSessionByUserId(int userId) {
        try (Connection conn = dataSources.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM sessione WHERE id_utente = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Sessione sessione = new Sessione();
                        sessione.setId(rs.getInt("Id_Sessione"));
                        sessione.setUtenteId(rs.getInt("id_Utente"));
                        sessione.setData_Sessione(rs.getTimestamp("Data_Sessione"));
                        return Optional.of(sessione);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void delete(int sessionId) {
        try {
            try (Connection connection = dataSources.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM sessione WHERE Id_Sessione = ?")) {
                    statement.setInt(1, sessionId);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public Integer find(int idSessione) {
        try (Connection connsessione = dataSources.getConnection()) {
            try (PreparedStatement statement = connsessione.prepareStatement("SELECT id_utente FROM sessione WHERE Id_Sessione = ?")) {
                statement.setInt(1, idSessione);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int idUtente = resultSet.getInt("id_utente");
                        return idUtente;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Cliente findCategory(int sessionId) {
        try (Connection connection = dataSources.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM utente WHERE Id_Utente = ?")) {
                statement.setInt(1, sessionId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Cliente cliente = new Cliente();
                        cliente.setId(resultSet.getInt("Id_Utente"));
                        cliente.setTipologia(resultSet.getString("Tipologia"));
                        return cliente;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
