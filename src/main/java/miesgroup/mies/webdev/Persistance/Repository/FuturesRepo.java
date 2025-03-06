package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Future;

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

    public List<Future> findByYear(String date) throws SQLException {
        String query = "SELECT fy.year, NULL, NULL, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN yearly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Future> futuresList = new ArrayList<>();
                while (resultSet.next()) {
                    futuresList.add(new Future(null, resultSet.getDouble(4), resultSet.getString(1), null, null));
                }
                return futuresList;
            }
        }
    }

    public List<Future> findByQuarter(String date) throws SQLException {
        String query = "SELECT fy.year, fy.quarter, NULL, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN quarterly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Future> futuresList = new ArrayList<>();
                while (resultSet.next()) {
                    futuresList.add(new Future(null, resultSet.getDouble(4), resultSet.getString(1), null, resultSet.getString(2)));
                }
                return futuresList;
            }
        }
    }

    public List<Future> findByMonth(String date) throws SQLException {
        String query = "SELECT fy.year, NULL, fy.month, fe.settlementPrice " +
                "FROM futures_eex fe " +
                "JOIN monthly_futures fy ON fe.id = fy.id " +
                "WHERE fe.date = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Future> futuresList = new ArrayList<>();
                while (resultSet.next()) {
                    futuresList.add(new Future(null, resultSet.getDouble(4), resultSet.getString(1), resultSet.getString(3), null));
                }
                return futuresList;
            }
        }
    }

    public String getLastDateFromYearlyFutures() throws SQLException {
        String query = "SELECT MAX(fe.date) FROM futures_eex fe " +
                "JOIN yearly_futures yf ON fe.id = yf.id " +
                "WHERE fe.date IS NOT NULL";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next() && resultSet.getDate(1) != null) {
                return new SimpleDateFormat("yyyy-MM-dd").format(resultSet.getDate(1));
            }
        }
        return null;
    }
}
