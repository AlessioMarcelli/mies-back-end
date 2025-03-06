package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.AlertData;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Rest.Model.Futures;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Service.LoggerService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Optional;


@ApplicationScoped
public class ClienteRepo implements PanacheRepositoryBase<Cliente, Integer> {
    private final DataSource dataSource;
    private final LoggerService loggerService;

    public ClienteRepo(DataSource dataSources, LoggerService loggerService) {
        this.dataSource = dataSources;
        this.loggerService = loggerService;
    }


    public boolean existsByUsername(String username) {
        return count("Username", username) > 0;
    }

    public void insert(Cliente nuovoCliente) {
        nuovoCliente.persist();
    }

    public Optional<Cliente> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<Cliente> findByUsernamelAndPasswordHash(String username, String password) {
        return find("username = ?1 and password = ?2", username, password).firstResultOptional();
    }

    public String getClasseAgevolazioneByPod(String idPod) {
        Pod p = Pod.find("id", idPod).firstResult();
        if (p == null) {
            return null;
        }
        Cliente c = p.getUtente();
        if (c == null) {
            return null;
        }
        return c.getClasseAgevolazione();
    }


    public Cliente getCliente(Integer idUtente) {
        return findById(idUtente);
    }

    public boolean updateCliente(int idUtente, String field, String newValue) {
        Cliente cliente = findById(idUtente);
        if (cliente == null) {
            return false;
        }

        switch (field) {
            case "username":
                cliente.setUsername(newValue);
                break;
            case "password":
                cliente.setPassword(newValue);
                break;
            case "sedeLegale":
                cliente.setSedeLegale(newValue);
                break;
            case "pIva":
                cliente.setpIva(newValue);
                break;
            case "stato":
                cliente.setStato(newValue);
                break;
            case "email":
                cliente.setEmail(newValue);
                break;
            case "telefono":
                cliente.setTelefono(newValue);
                break;
            case "tipologia":
                cliente.setTipologia(newValue);
                break;
            case "classeAgevolazione":
                cliente.setClasseAgevolazione(newValue);
                break;
            case "codiceAteco":
                cliente.setCodiceAteco(newValue);
                break;
            case "energivori":
                cliente.setEnergivori(Boolean.parseBoolean(newValue));
                break;
            case "gassivori":
                cliente.setGassivori(Boolean.parseBoolean(newValue));
                break;
            case "consumoAnnuoEnergia":
                try {
                    cliente.setConsumoAnnuoEnergia(Float.parseFloat(newValue));
                } catch (NumberFormatException e) {
                    return false; // Fallisce se il valore non è un numero valido
                }
                break;
            case "fatturatoAnnuo":
                try {
                    cliente.setFatturatoAnnuo(Float.parseFloat(newValue));
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            default:
                return false; // Campo non valido
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


