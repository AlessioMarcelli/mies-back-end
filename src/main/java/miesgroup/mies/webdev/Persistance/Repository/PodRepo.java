package miesgroup.mies.webdev.Persistance.Repository;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Pod;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
public class PodRepo {

    private final DataSource dataSources;

    public PodRepo(DataSource dataSources) {
        this.dataSources = dataSources;
    }

    public void insert(Pod newPod) {
        try (Connection connection = dataSources.getConnection();) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO pod (Id_Pod, Tensione_Alimentazione, Potenza_Impegnata, Potenza_Disponibile, id_utente) VALUES (?, ?, ?, ?, ?)");) {
                statement.setString(1, newPod.getId());
                statement.setDouble(2, newPod.getTensione_Alimentazione());
                statement.setDouble(3, newPod.getPotenza_Impegnata());
                statement.setDouble(4, newPod.getPotenza_Disponibile());
                statement.setInt(5, newPod.getId_utente());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Pod> findAll(int id_utente) {
        try (Connection connection = dataSources.getConnection();) {
            //Query per selezionare tutti i pod
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT Id_Pod,Tensione_Alimentazione,Potenza_Impegnata,Potenza_Disponibile,Sede,Nazione FROM pod WHERE id_utente = ?");) {
                statement.setInt(1, id_utente);
                ResultSet resultSet = statement.executeQuery();
                ArrayList<Pod> elenco = new ArrayList<>();
                while (resultSet.next()) {
                    Pod pod = new Pod();
                    pod.setId(resultSet.getString("Id_Pod"));
                    pod.setTensione_Alimentazione(resultSet.getDouble("Tensione_Alimentazione"));
                    pod.setPotenza_Impegnata(resultSet.getDouble("Potenza_Impegnata"));
                    pod.setPotenza_Disponibile(resultSet.getDouble("Potenza_Disponibile"));
                    pod.setSede(resultSet.getString("Sede"));
                    pod.setNazione(resultSet.getString("Nazione"));
                    elenco.add(pod);
                }
                return elenco;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //devo far ritonare una lista in quanto potrebbero essere mostrati più risultati
    public Pod cercaIdPod(String id, int id_utente) {
        try (Connection connection = dataSources.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT Id_Pod,Tensione_Alimentazione,Potenza_Impegnata,Potenza_Disponibile,Sede,Nazione FROM pod WHERE Id_Pod = ? AND id_utente = ?")) {
                statement.setString(1, id);
                statement.setInt(2, id_utente);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    Pod pod = new Pod();
                    pod.setId(resultSet.getString("Id_Pod"));
                    pod.setTensione_Alimentazione(resultSet.getDouble("Tensione_Alimentazione"));
                    pod.setPotenza_Impegnata(resultSet.getDouble("Potenza_Impegnata"));
                    pod.setPotenza_Disponibile(resultSet.getDouble("Potenza_Disponibile"));
                    pod.setSede(resultSet.getString("Sede"));
                    pod.setNazione(resultSet.getString("Nazione"));
                    return pod;
                }
                return null;

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
