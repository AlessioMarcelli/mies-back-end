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

    public int insert(PDFFile pdfFile) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // Controlla se il nome del file esiste già
            try (PreparedStatement checkStatement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM filepdf WHERE File_Name = ?")) {
                checkStatement.setString(1, pdfFile.getFile_Name());
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        throw new RuntimeException("File name already exists in the database");
                    }
                }
            }
            // Inserisci il nuovo file
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
        return pdfFile.getId_File();
    }


    public void abbinaPod(int idFile, String idPod) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE filepdf SET id_pod = ? WHERE id_File = ?")) {
                statement.setString(1, idPod);
                statement.setInt(2, idFile);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating file in database", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database", e);
        }
    }
}
