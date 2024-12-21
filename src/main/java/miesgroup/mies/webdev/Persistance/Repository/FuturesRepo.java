package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Rest.Model.Futures;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.List;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
public class FuturesRepo {

    private final DataSource dataSource;

    public FuturesRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Metodo per ottenere i futures annuali
    public List<Futures> findByYear(String date) throws SQLException {
        String query = "SELECT fy.year, NULL, NULL, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN yearly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, Date.valueOf(date)); // Convertire la data in java.sql.Date

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Futures> futuresList = new ArrayList<>();
                while (resultSet.next()) {
                    Futures futures = new Futures(
                            resultSet.getString(1),    // year
                            resultSet.getString(2), // quarter (null in this case)
                            resultSet.getString(3), // month (null in this case)
                            resultSet.getDouble(4)  // settlementPrice
                    );
                    futuresList.add(futures);
                }
                return futuresList;
            }
        }
    }

    // Metodo per ottenere i futures trimestrali
    public List<Futures> findByQuarter(String date) throws SQLException {
        String query = "SELECT fy.year, fy.quarter, NULL, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN quarterly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the date parameter in the query
            statement.setDate(1, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Futures> futuresList = new ArrayList<>();

                // Iterate over the result set
                while (resultSet.next()) {
                    // Create Futures object with mapped values
                    Futures futures = new Futures(
                            resultSet.getString(1),  // year
                            resultSet.getString(2),  // quarter
                            null,                    // month is null
                            resultSet.getDouble(4)   // settlementPrice
                    );
                    futuresList.add(futures);
                }

                return futuresList;
            }
        } catch (SQLException e) {
            // Handle exception and rethrow or log it
            throw new SQLException("Error executing query to find futures by quarter", e);
        }
    }

    // Metodo per ottenere i futures mensili
    public List<Futures> findByMonth(String date) throws SQLException {
        String query = "SELECT fy.year, NULL, fy.month, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN monthly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Set the date parameter in the query
            statement.setDate(1, Date.valueOf(date));

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Futures> futuresList = new ArrayList<>();

                // Iterate over the result set
                while (resultSet.next()) {
                    // Create Futures object with mapped values
                    Futures futures = new Futures(
                            resultSet.getString(1),// year
                            null,                             // quarter
                            resultSet.getString(3),// month is null
                            resultSet.getDouble(4)  // settlementPrice
                    );
                    futuresList.add(futures);
                }

                return futuresList;
            }
        } catch (SQLException e) {
            // Handle exception and rethrow or log it
            throw new SQLException("Error executing query to find futures by quarter", e);
        }
    }

    // Metodo per ottenere l'ultima data dalla tabella 'yearly_futures'
    public String getLastDateFromYearlyFutures() throws SQLException {
        String query = "SELECT MAX(fe.date) " +
                "FROM futures_eex fe " +
                "JOIN yearly_futures yf ON fe.id = yf.id " +
                "WHERE fe.date IS NOT NULL";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                Date sqlDate = resultSet.getDate(1);
                if (sqlDate != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return dateFormat.format(sqlDate); // Restituisce la data come stringa
                }
            }
        }
        return null; // Gestire il caso in cui non ci sono risultati
    }
}