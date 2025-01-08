package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Model.Periodo;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Collections;
import java.util.Map;

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

    public PDFFile findById(int id) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT Id_File, File_Name, file_Data FROM filepdf WHERE Id_File = ?")) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    PDFFile pdfFile = new PDFFile();
                    pdfFile.setId_File(resultSet.getInt("id_File"));
                    pdfFile.setFile_Name(resultSet.getString("File_Name"));
                    pdfFile.setFile_Data(resultSet.getBytes("file_Data"));
                    return pdfFile;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public byte[] getFile(int id) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT file_Data FROM filepdf WHERE Id_File = ?")) {
                statement.setInt(1, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getBytes("file_Data");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void saveDataToDatabase(
            Map<String, Map<String, Map<String, Integer>>> lettureMese,
            Map<String, Double> spese,
            String idPod,
            String nomeBolletta,
            Periodo periodo
    ) {
        String insertQuery = """
                    INSERT INTO bolletta_pod (
                        id_pod, Nome_Bolletta, F1_Attiva, F2_Attiva, F3_Attiva,
                        F1_Reattiva, F2_Reattiva, F3_Reattiva, F1_Potenza, F2_Potenza, F3_Potenza,
                        Spese_Energia, Spese_Trasporto, Oneri, Imposte, Periodo_Inizio, Periodo_Fine,
                        Mese, TOT_Attiva, TOT_Reattiva, Anno
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(insertQuery)) {

            for (Map.Entry<String, Map<String, Map<String, Integer>>> meseEntry : lettureMese.entrySet()) {
                String mese = meseEntry.getKey();
                Map<String, Map<String, Integer>> categorie = meseEntry.getValue();

                // Estrazione dei consumi per categoria
                Double f1Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F1");
                Double f2Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F2");
                Double f3Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F3");
                Double f1Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F1");
                Double f2Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F2");
                Double f3Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F3");
                Double f1Potenza = getCategoriaConsumo(categorie, "Potenza", "F1");
                Double f2Potenza = getCategoriaConsumo(categorie, "Potenza", "F2");
                Double f3Potenza = getCategoriaConsumo(categorie, "Potenza", "F3");

                // Estrazione delle spese
                Double spesaEnergia = spese.getOrDefault("Materia Energia", 0.0);
                Double spesaTrasporto = spese.getOrDefault("Trasporto e Gestione Contatore", 0.0);
                Double oneri = spese.getOrDefault("Oneri di Sistema", 0.0);
                Double imposte = spese.getOrDefault("Totale Imposte", 0.0);

                // Calcolo dei totali
                Double totAttiva = (f1Attiva + f2Attiva + f3Attiva);
                Double totReattiva = (f1Reattiva + f2Reattiva + f3Reattiva);

                // Imposta i parametri nella query
                stmt.setString(1, idPod);
                stmt.setString(2, nomeBolletta);
                stmt.setBigDecimal(3, BigDecimal.valueOf(f1Attiva != null ? f1Attiva : 0.0));
                stmt.setBigDecimal(4, BigDecimal.valueOf(f2Attiva != null ? f2Attiva : 0.0));
                stmt.setBigDecimal(5, BigDecimal.valueOf(f3Attiva != null ? f3Attiva : 0.0));
                stmt.setBigDecimal(6, BigDecimal.valueOf(f1Reattiva != null ? f1Reattiva : 0.0));
                stmt.setBigDecimal(7, BigDecimal.valueOf(f2Reattiva != null ? f2Reattiva : 0.0));
                stmt.setBigDecimal(8, BigDecimal.valueOf(f3Reattiva != null ? f3Reattiva : 0.0));
                stmt.setBigDecimal(9, BigDecimal.valueOf(f1Potenza != null ? f1Potenza : 0.0));
                stmt.setBigDecimal(10, BigDecimal.valueOf(f2Potenza != null ? f2Potenza : 0.0));
                stmt.setBigDecimal(11, BigDecimal.valueOf(f3Potenza != null ? f3Potenza : 0.0));
                stmt.setBigDecimal(12, BigDecimal.valueOf(spesaEnergia));
                stmt.setBigDecimal(13, BigDecimal.valueOf(spesaTrasporto));
                stmt.setBigDecimal(14, BigDecimal.valueOf(oneri));
                stmt.setBigDecimal(15, BigDecimal.valueOf(imposte));
                stmt.setDate(16, new java.sql.Date(periodo.getInizio().getTime()));
                stmt.setDate(17, new java.sql.Date(periodo.getFine().getTime()));
                stmt.setString(18, mese);
                stmt.setBigDecimal(19, BigDecimal.valueOf(totAttiva));
                stmt.setBigDecimal(20, BigDecimal.valueOf(totReattiva));
                stmt.setString(21, periodo.getAnno());

                // Aggiunge il batch
                stmt.addBatch();
            }

            // Esegue il batch
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Errore durante il salvataggio dei dati nel database.", e);
        }
    }

    private Double getCategoriaConsumo(Map<String, Map<String, Integer>> categorie, String categoria, String fascia) {
        return categorie.getOrDefault(categoria, Collections.emptyMap()).getOrDefault(fascia, 0).doubleValue();
    }

}
