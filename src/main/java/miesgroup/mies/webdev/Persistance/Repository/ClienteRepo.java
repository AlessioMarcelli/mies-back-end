package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Service.HashCalculator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class ClienteRepo {
    private final DataSource dataSources;

    public ClienteRepo(DataSource dataSources) {
        this.dataSources = dataSources;
    }

    public boolean existsByUsername(String username) {
        try (Connection connection = dataSources.getConnection();
             //Query per controllare se nel database esiste email
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM utente WHERE Username = ?")) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void insert(Cliente nuovoCliente) {
        try (Connection connection = dataSources.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO utente (Username, Password, Sede_Legale, Piva, Email, Telefono, Stato, Tipologia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, nuovoCliente.getUsername());
                statement.setString(2, nuovoCliente.getPassword());
                statement.setString(3, nuovoCliente.getSedeLegale());
                statement.setString(4, nuovoCliente.getpIva());
                statement.setString(5, nuovoCliente.getEmail());
                statement.setString(6, nuovoCliente.getTelefono());
                statement.setString(7, nuovoCliente.getStato());
                statement.setString(8, nuovoCliente.getTipologia());
                statement.executeUpdate();
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    nuovoCliente.setId(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Cliente> findByUsername(String username) {
        try (Connection conn = dataSources.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM utente WHERE Username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Cliente utente = new Cliente();
                        utente.setId(rs.getInt("Id_Utente"));
                        utente.setUsername(rs.getString("Username"));
                        utente.setpIva(rs.getString("Piva"));
                        utente.setEmail(rs.getString("Email"));
                        utente.setPassword(rs.getString("Password"));
                        utente.setSedeLegale(rs.getString("Sede_Legale"));
                        utente.setTelefono(rs.getString("Telefono"));
                        utente.setStato(rs.getString("Stato"));
                        utente.setTipologia(rs.getString("Tipologia"));
                        return Optional.of(utente);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<Cliente> findByUsernamelAndPasswordHash(String username, String password) {
        try {
            try (Connection connection = dataSources.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT Id_Utente, Username FROM utente WHERE Username = ? AND Password = ?")) {
                    statement.setString(1, username);
                    statement.setString(2, password);
                    var resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        Cliente cliente = new Cliente();
                        cliente.setId(resultSet.getInt("Id_Utente"));
                        cliente.setUsername(resultSet.getString("Username"));
                        return Optional.of(cliente);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public int idUtenteDaIdSessione(int sessionId) {
        try (Connection connection = dataSources.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT ID_Utente FROM sessione WHERE ID_Sessione = ?")) {
                statement.setInt(1, sessionId);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt("ID_Utente");
                } else {
                    throw new EntityNotFoundException("Non è presente alcuna sessione con id " + sessionId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
