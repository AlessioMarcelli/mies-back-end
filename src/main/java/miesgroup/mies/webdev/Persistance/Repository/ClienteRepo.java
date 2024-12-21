package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
public class ClienteRepo {
    private final DataSource dataSource;

    public ClienteRepo(DataSource dataSources) {
        this.dataSource = dataSources;
    }

    public boolean existsByUsername(String username) {
        try (Connection connection = dataSource.getConnection();
             //Query per controllare se nel database esiste email
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM utente WHERE Username = ?")) {
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
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO utente (Username, Password, Sede_Legale, Piva, Email, Telefono, Stato, Tipologia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
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
        try (Connection conn = dataSource.getConnection()) {
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
            try (Connection connection = dataSource.getConnection()) {
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

    public String getClasseAgevolazioneByPod(String idPod) {
        String queryIdUtente = "SELECT Id_Utente FROM pod WHERE Id_Pod = ?";
        String queryClasseAgevolazione = "SELECT Classe_Agevolazione FROM utente WHERE Id_Utente = ?";

        try (Connection connection = dataSource.getConnection()) {
            // Primo step: ottenere id_utente
            Integer idUtente = null;
            try (PreparedStatement statementIdUtente = connection.prepareStatement(queryIdUtente)) {
                statementIdUtente.setString(1, idPod);
                try (ResultSet resultSet = statementIdUtente.executeQuery()) {
                    if (resultSet.next()) {
                        idUtente = resultSet.getInt("Id_Utente");
                    }
                }
            }

            // Se non si trova l'id_utente, ritorna null
            if (idUtente == null) {
                return null;
            }

            // Secondo step: ottenere classe_agevolazione
            try (PreparedStatement statementClasseAgevolazione = connection.prepareStatement(queryClasseAgevolazione)) {
                statementClasseAgevolazione.setInt(1, idUtente);
                try (ResultSet resultSet = statementClasseAgevolazione.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("Classe_Agevolazione");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying database", e);
        }

        return null;
    }


    public Cliente getCliente(Integer integer) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Username,Piva,Email,Sede_Legale,Telefono,Stato,Tipologia FROM utente WHERE Id_Utente = ?")) {
                statement.setInt(1, integer);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Cliente cliente = new Cliente();
                        cliente.setUsername(resultSet.getString("Username"));
                        cliente.setpIva(resultSet.getString("Piva"));
                        cliente.setEmail(resultSet.getString("Email"));
                        cliente.setSedeLegale(resultSet.getString("Sede_Legale"));
                        cliente.setTelefono(resultSet.getString("Telefono"));
                        cliente.setStato(resultSet.getString("Stato"));
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

    public void updateCliente(int idUtente, String field, String newValue) {
        // Lista dei campi permessi
        Set<String> validFields = Set.of(
                "username",
                "password",
                "sedeLegale",
                "pIva",
                "stato",
                "email",
                "telefono",
                "classeAgevolazione");

        if (!validFields.contains(field)) {
            throw new IllegalArgumentException("Campo non valido: " + field);
        }

        String query = "UPDATE utente SET " + field + " = ? WHERE Id_Utente = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newValue);
            statement.setInt(2, idUtente);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
