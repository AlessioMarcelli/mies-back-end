package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class FileRepo {

    private final DataSource dataSource;

    public FileRepo(DataSource dataSources) {
        this.dataSource = dataSources;
    }

    public void insert(PDFFile pdfFile) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO filepdf (File_Name, file_Data) VALUES (?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, pdfFile.getFile_Name());
                statement.setBytes(2, pdfFile.getFile_Data());
                statement.executeUpdate();
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        pdfFile.setId_File(id);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error inserting file into database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }

/*    public PDFFile find(int id) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT Id_File, File_Name, file_Data FROM filepdf WHERE Id_File = ?")) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        PDFFile pdfFile = new PDFFile();
                        pdfFile.setId_File(resultSet.getInt("Id_File"));
                        pdfFile.setFile_Name(resultSet.getString("File_Name"));
                        pdfFile.setFile_Data(resultSet.getBytes("file_Data"));
                        return pdfFile;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }*/

}
