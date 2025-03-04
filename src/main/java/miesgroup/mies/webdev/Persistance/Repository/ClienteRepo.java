package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.AlertData;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Rest.Model.Futures;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM utente WHERE Id_Utente = ?")) {
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

    public boolean updateCliente(int idUtente, String field, String newValue) {
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
        int rowsAffected = 0;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, newValue);
            statement.setInt(2, idUtente);

            rowsAffected = statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Ritorna false in caso di eccezione
        }
        return rowsAffected > 0; // Ritorna true se almeno una riga è stata aggiornata
    }
    public boolean checkEmailStatus(int idUtente, boolean checkEmail) {
        String checkQuery = "SELECT checkEmail FROM utente WHERE Id_Utente = ?";
        String updateQuery = "UPDATE utente SET checkEmail = ? WHERE Id_Utente = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {

            checkStatement.setInt(1, idUtente);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next()) {
                    Boolean checkEmailDB = resultSet.getObject("checkEmail", Boolean.class);

                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        if (checkEmailDB == null) {
                            // Se il valore è NULL nel database, usa quello dal frontend
                            updateStatement.setBoolean(1, checkEmail);
                        } else {
                            // Se il valore esiste, aggiorna con quello nuovo
                            updateStatement.setBoolean(1, !checkEmailDB);
                        }
                        updateStatement.setInt(2, idUtente);
                        updateStatement.executeUpdate();
                    }
                    return true; // Operazione riuscita
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Operazione fallita
    }
    public Boolean getCheckEmailStatus(int idUtente) {
        String query = "SELECT checkEmail FROM utente WHERE Id_Utente = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idUtente);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getObject("checkEmail", Boolean.class); // Può essere true, false o null
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Se l'utente non esiste o si verifica un errore
    }

    public Map<String, Boolean> checkUserAlert(int idUtente, String futuresType) {
        String baseQuery = """
        SELECT 
            'MonthlyAlert' AS table_name, EXISTS (
                SELECT 1 FROM MonthlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record
        UNION ALL
        SELECT 
            'QuarterlyAlert' AS table_name, EXISTS (
                SELECT 1 FROM QuarterlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record
        UNION ALL
        SELECT 
            'YearlyAlert' AS table_name, EXISTS (
                SELECT 1 FROM YearlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record;
    """;

        Map<String, Boolean> resultMap = new HashMap<>();

        // Filtra la query in base al valore di futuresType
        String query = switch (futuresType) {
            case "Yearly" -> """
            SELECT 
                'YearlyAlert' AS table_name, EXISTS (
                    SELECT 1 FROM YearlyAlert WHERE Id_Utente = ? LIMIT 1
                ) AS has_record;
        """;
            case "Quarterly" -> """
            SELECT 
                'QuarterlyAlert' AS table_name, EXISTS (
                    SELECT 1 FROM QuarterlyAlert WHERE Id_Utente = ? LIMIT 1
                ) AS has_record;
        """;
            case "Monthly" -> """
            SELECT 
                'MonthlyAlert' AS table_name, EXISTS (
                    SELECT 1 FROM MonthlyAlert WHERE Id_Utente = ? LIMIT 1
                ) AS has_record;
        """;
            case "All" -> baseQuery; // Esegue il controllo su tutte le tabelle
            case "General" -> """
            SELECT 
                'GeneralAlert' AS table_name, EXISTS (
                    SELECT 1 FROM GeneralAlert WHERE Id_Utente = ? LIMIT 1
                )  AS has_record;
        """;
            default -> throw new IllegalArgumentException("Tipo di futures non valido: " + futuresType);
        };

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Imposta il parametro Id_Utente
            if (futuresType.equals("All")) {
                statement.setInt(1, idUtente);
                statement.setInt(2, idUtente);
                statement.setInt(3, idUtente);
            } else {
                statement.setInt(1, idUtente);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("table_name");
                    boolean hasRecord = resultSet.getBoolean("has_record");
                    resultMap.put(tableName, hasRecord);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    public Map<String, Boolean> deleteUserAlert(int idUtente, String futuresType) {
        Map<String, Boolean> resultMap = new HashMap<>();
        boolean deleteSuccessful = false;

        try (Connection connection = dataSource.getConnection()) {
            // Controlla il tipo di futures e costruisci la query appropriata
            switch (futuresType) {
                case "Yearly" -> {
                    deleteSuccessful = executeDelete(connection, "DELETE FROM YearlyAlert WHERE Id_Utente = ?", idUtente);
                    resultMap.put("YearlyAlert", deleteSuccessful);
                }
                case "Quarterly" -> {
                    deleteSuccessful = executeDelete(connection, "DELETE FROM QuarterlyAlert WHERE Id_Utente = ?", idUtente);
                    resultMap.put("QuarterlyAlert", deleteSuccessful);
                }
                case "Monthly" -> {
                    deleteSuccessful = executeDelete(connection, "DELETE FROM MonthlyAlert WHERE Id_Utente = ?", idUtente);
                    resultMap.put("MonthlyAlert", deleteSuccessful);
                }
                case "General" -> {
                    deleteSuccessful = executeDelete(connection, "DELETE FROM GeneralAlert WHERE Id_Utente = ?", idUtente);
                    resultMap.put("GeneralAlert", deleteSuccessful);
                }
                case "All" -> {
                    boolean monthlyDeleted = executeDelete(connection, "DELETE FROM MonthlyAlert WHERE Id_Utente = ?", idUtente);
                    boolean quarterlyDeleted = executeDelete(connection, "DELETE FROM QuarterlyAlert WHERE Id_Utente = ?", idUtente);
                    boolean yearlyDeleted = executeDelete(connection, "DELETE FROM YearlyAlert WHERE Id_Utente = ?", idUtente);

                    resultMap.put("MonthlyAlert", monthlyDeleted);
                    resultMap.put("QuarterlyAlert", quarterlyDeleted);
                    resultMap.put("YearlyAlert", yearlyDeleted);

                    deleteSuccessful = monthlyDeleted || quarterlyDeleted || yearlyDeleted;
                }
                default -> throw new IllegalArgumentException("Tipo di futures non valido: " + futuresType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    // Metodo di supporto per eseguire le DELETE
    private boolean executeDelete(Connection connection, String query, int idUtente) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idUtente);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public Map<String, Boolean> checkAlertStates(int idUtente) {
        // Crea una mappa per memorizzare lo stato di ogni tipo di alert
        Map<String, Boolean> alertStates = new HashMap<>();

        // Controlla ogni tipo di alert e aggiungi il risultato alla mappa
        alertStates.put("MonthlyAlert", checkUserAlert(idUtente, "Monthly").getOrDefault("MonthlyAlert", false));
        alertStates.put("QuarterlyAlert", checkUserAlert(idUtente, "Quarterly").getOrDefault("QuarterlyAlert", false));
        alertStates.put("YearlyAlert", checkUserAlert(idUtente, "Yearly").getOrDefault("YearlyAlert", false));
        alertStates.put("GeneralAlert", checkUserAlert(idUtente, "General").getOrDefault("GeneralAlert", false));

        return alertStates; // Restituisce la mappa con lo stato degli alert
    }

    public boolean updateDataFuturesAlert(int idUtente, String futuresType, double[] maxPriceValue, double[] minPriceValue, String[] frequency, boolean[] checkModality, boolean checkEmail) {
        // Prima, controlla se i dati esistono
        Map<String, Boolean> alertExistence = checkUserAlert(idUtente, futuresType);

        boolean inserted = false;
        String query;

        try (Connection connection = dataSource.getConnection()) {

            if ("All".equals(futuresType)){
                // Se il futuresType è "All", dobbiamo controllare tutte le tabelle
                boolean monthlyExists = alertExistence.get("MonthlyAlert");
                boolean quarterlyExists = alertExistence.get("QuarterlyAlert");
                boolean yearlyExists = alertExistence.get("YearlyAlert");
                // Gestisci "MonthlyAlert"
                if (monthlyExists) {
                    query = "UPDATE MonthlyAlert SET maxPriceValue = ?, minPriceValue = ?, frequencyA = ?, checkModality = ? WHERE Id_Utente = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[2]);
                        statement.setDouble(2, minPriceValue[2]);
                        statement.setString(3, frequency[2]);
                        statement.setBoolean(4, checkModality[2]);
                        statement.setInt(5, idUtente);
                        int rowsUpdated = statement.executeUpdate();
                        inserted = rowsUpdated > 0;
                    }
                } else {
                    query = "INSERT INTO MonthlyAlert (maxPriceValue, minPriceValue, Id_Utente, frequencyA, checkModality) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[2]);
                        statement.setDouble(2, minPriceValue[2]);
                        statement.setInt(3, idUtente);
                        statement.setString(4, frequency[2]);
                        statement.setBoolean(5, checkModality[2]);
                        int rowsInserted = statement.executeUpdate();
                        inserted = rowsInserted > 0;
                    }
                }

                // Gestisci "QuarterlyAlert"
                if (quarterlyExists) {
                    query = "UPDATE QuarterlyAlert SET maxPriceValue = ?, minPriceValue = ?, frequencyA = ?, checkModality = ? WHERE Id_Utente = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[1]);
                        statement.setDouble(2, minPriceValue[1]);
                        statement.setString(3, frequency[1]);
                        statement.setBoolean(4, checkModality[1]);
                        statement.setInt(5, idUtente);
                        int rowsUpdated = statement.executeUpdate();
                        inserted = rowsUpdated > 0;
                    }
                } else {
                    query = "INSERT INTO QuarterlyAlert (maxPriceValue, minPriceValue, Id_Utente, frequencyA, checkModality) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[1]);
                        statement.setDouble(2, minPriceValue[1]);
                        statement.setInt(3, idUtente);
                        statement.setString(4, frequency[1]);
                        statement.setBoolean(5, checkModality[1]);
                        int rowsInserted = statement.executeUpdate();
                        inserted = rowsInserted > 0;
                    }
                }

                // Gestisci "YearlyAlert"
                if (yearlyExists) {
                    query = "UPDATE YearlyAlert SET maxPriceValue = ?, minPriceValue = ?, frequencyA = ?, checkModality = ? WHERE Id_Utente = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[0]);
                        statement.setDouble(2, minPriceValue[0]);
                        statement.setString(3, frequency[0]);
                        statement.setBoolean(4, checkModality[0]);
                        statement.setInt(5, idUtente);
                        int rowsUpdated = statement.executeUpdate();
                        inserted = rowsUpdated > 0;
                    }
                } else {
                    query = "INSERT INTO YearlyAlert (maxPriceValue, minPriceValue, Id_Utente, frequencyA, checkModality) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[0]);
                        statement.setDouble(2, minPriceValue[0]);
                        statement.setInt(3, idUtente);
                        statement.setString(4, frequency[0]);
                        statement.setBoolean(5, checkModality[0]);
                        int rowsInserted = statement.executeUpdate();
                        inserted = rowsInserted > 0;
                    }
                }

                checkEmailStatus(idUtente, checkEmail);

            } else {
                // In caso di "Monthly", "Quarterly", "Yearly", esegui l'inserimento o aggiornamento solo per quella tabella
                String table = futuresType + "Alert";
                boolean exists = alertExistence.getOrDefault(table, false);

                if (exists) {
                    query = "UPDATE " + table + " SET maxPriceValue = ?, minPriceValue = ?, frequencyA = ?, checkModality = ? WHERE Id_Utente = ?";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[0]);
                        statement.setDouble(2, minPriceValue[0]);
                        statement.setString(3, frequency[0]);
                        statement.setBoolean(4, checkModality[0]);
                        statement.setInt(5, idUtente);
                        int rowsUpdated = statement.executeUpdate();
                        inserted = rowsUpdated > 0;
                    }
                } else {
                    query = "INSERT INTO " + table + " (maxPriceValue, minPriceValue, Id_Utente, frequencyA, checkModality) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setDouble(1, maxPriceValue[0]);
                        statement.setDouble(2, minPriceValue[0]);
                        statement.setInt(3, idUtente);
                        statement.setString(4, frequency[0]);
                        statement.setBoolean(5, checkModality[0]);
                        int rowsInserted = statement.executeUpdate();
                        inserted = rowsInserted > 0;
                    }
                }
                checkEmailStatus(idUtente, checkEmail);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inserted;
    }

    public AlertData[] checkUserAlertFillField(int idUtente) {
        System.out.println("Starting method: checkUserAlertForFillField");
        System.out.println("Id_Utente: " + idUtente);

        String generalAlertQuery = """
        SELECT 
            'GeneralAlert' AS table_name, 
            EXISTS (
                SELECT 1 FROM GeneralAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record;
    """;

        String monthlyAlertQuery = """
        SELECT 
            'MonthlyAlert' AS table_name, 
            EXISTS (
                SELECT 1 FROM MonthlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record;
    """;

        String quarterlyAlertQuery = """
        SELECT 
            'QuarterlyAlert' AS table_name, 
            EXISTS (
                SELECT 1 FROM QuarterlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record;
    """;

        String yearlyAlertQuery = """
        SELECT 
            'YearlyAlert' AS table_name, 
            EXISTS (
                SELECT 1 FROM YearlyAlert WHERE Id_Utente = ? LIMIT 1
            ) AS has_record;
    """;

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connection established.");

            // Controllo GeneralAlert
            try (PreparedStatement generalStatement = connection.prepareStatement(generalAlertQuery)) {
                generalStatement.setInt(1, idUtente);
                System.out.println("Checking GeneralAlert...");

                try (ResultSet resultSet = generalStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getBoolean("has_record")) {
                        System.out.println("Record found in GeneralAlert. Fetching data...");
                        return new AlertData[]{fetchAlertData(connection, "GeneralAlert", idUtente)};
                    }
                }
            }
            // Controllo MonthlyAlert
            AlertData[] futuresAlertData = new AlertData[3];

            try (PreparedStatement monthlyStatement = connection.prepareStatement(monthlyAlertQuery)) {
                monthlyStatement.setInt(1, idUtente);
                System.out.println("Checking MonthlyAlert...");

                try (ResultSet resultSet = monthlyStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getBoolean("has_record")) {
                        System.out.println("Record found in MonthlyAlert. Fetching data...");
                        futuresAlertData[0] = fetchAlertData(connection, "MonthlyAlert", idUtente);
                    }
                }
            }

            // Controllo QuarterlyAlert
            try (PreparedStatement quarterlyStatement = connection.prepareStatement(quarterlyAlertQuery)) {
                quarterlyStatement.setInt(1, idUtente);
                System.out.println("Checking QuarterlyAlert...");

                try (ResultSet resultSet = quarterlyStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getBoolean("has_record")) {
                        System.out.println("Record found in QuarterlyAlert. Fetching data...");
                        futuresAlertData[1] = fetchAlertData(connection, "QuarterlyAlert", idUtente);
                    }
                }
            }

            // Controllo YearlyAlert
            try (PreparedStatement yearlyStatement = connection.prepareStatement(yearlyAlertQuery)) {
                yearlyStatement.setInt(1, idUtente);
                System.out.println("Checking YearlyAlert...");

                try (ResultSet resultSet = yearlyStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getBoolean("has_record")) {
                        System.out.println("Record found in YearlyAlert. Fetching data...");
                        futuresAlertData[2] = fetchAlertData(connection, "YearlyAlert", idUtente);
                    }
                }
            }

            // Logica di ritorno
            if (futuresAlertData[2] != null) {
                return futuresAlertData;
            }
            if (futuresAlertData[1] != null) {
                return new AlertData[]{futuresAlertData[1]};
            }
            if (futuresAlertData[0] != null) {
                return new AlertData[]{futuresAlertData[0]};
            }
        } catch (SQLException e) {
            System.err.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("No alerts found for Id_Utente: " + idUtente);
        return null;
    }

    /**
     * Metodo per recuperare i dati della tabella specificata.
     */
    private AlertData fetchAlertData(Connection connection, String tableName, int idUtente) throws SQLException {
        System.out.println("Starting method: fetchAlertData");
        System.out.println("Fetching data from table: " + tableName + " for Id_Utente: " + idUtente);

        String query = String.format("SELECT * FROM %s WHERE Id_Utente = ?", tableName);
        System.out.println("Query to execute: " + query);

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idUtente);
            System.out.println("Set Id_Utente in query: " + idUtente);

            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Query executed for table: " + tableName);

                if (resultSet.next()) {
                    System.out.println("Data found in table: " + tableName);

                    AlertData alertData = new AlertData();
                    alertData.setFuturesType(tableName);
                    alertData.setMaxPriceValue(resultSet.getDouble("maxPriceValue"));
                    alertData.setMinPriceValue(resultSet.getDouble("minPriceValue"));
                    alertData.setFrequencyA(resultSet.getString("frequencyA"));
                    alertData.setCheckModality(resultSet.getBoolean("checkModality"));
                    System.out.println(alertData.getFrequencyA());
                    System.out.println("Fetched data: " + alertData);
                    return alertData;
                } else {
                    System.out.println("No data found in table: " + tableName);
                }
            }
        }
        System.out.println("No data retrieved from table: " + tableName);
        return null;
    }



    public List<Cliente> getClientsCheckEmail() {
        System.out.println("Starting method: getClientsWithCheckEmail");

        String query = """
            SELECT Username, Email, Id_Utente
            FROM utente
            WHERE checkEmail = 1
        """;

        List<Cliente> clients = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Database connection established.");

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                System.out.println("Executing query: " + query);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Cliente cliente = new Cliente();
                        cliente.setUsername(resultSet.getString("Username"));
                        cliente.setEmail(resultSet.getString("Email"));
                        cliente.setId(resultSet.getInt("Id_Utente"));

                        clients.add(cliente);
                        System.out.println("Cliente aggiunto: " + cliente.getUsername());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLException occurred: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Total clients found: " + clients.size());
        return clients;
    }

}


