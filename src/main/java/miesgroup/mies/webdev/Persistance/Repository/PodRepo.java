package miesgroup.mies.webdev.Persistance.Repository;

import miesgroup.mies.webdev.Persistance.Pod;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PodRepo {

    private final DataSource dataSources;

    public PodRepo(DataSource dataSources) {
        this.dataSources = dataSources;
    }

    public void insert(Pod newPod) {
        try (Connection connection = dataSources.getConnection();) {
            //Query per inserire un nuovo pod
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO pod (Id, Tensione_Alimentazione, Potenza_Impegnata, Potenza_Disponibile) VALUES (?, ?, ?, ?, ?)");) {
                statement.setString(1, newPod.getId());
                statement.setDouble(2, newPod.getTensione_Alimentazione());
                statement.setDouble(3, newPod.getPotenza_Impegnata());
                statement.setDouble(4, newPod.getPotenza_Disponibile());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Pod> findAll() {
        try (Connection connection = dataSources.getConnection();) {
            //Query per selezionare tutti i pod
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT Id_Pod,Tensione_Alimentazione,Potenza_Impegnata,Potenza_Disponibile FROM pod");) {
                ResultSet resultSet = statement.executeQuery();
                ArrayList<Pod> elenco = new ArrayList<>();
                while (resultSet.next()) {
                    Pod pod = new Pod();
                    pod.setId(resultSet.getString("Id_Pod"));
                    pod.setTensione_Alimentazione(resultSet.getDouble("Tensione_Alimentazione"));
                    pod.setPotenza_Impegnata(resultSet.getDouble("Potenza_Impegnata"));
                    pod.setPotenza_Disponibile(resultSet.getDouble("Potenza_Disponibile"));
                    elenco.add(pod);
                }
                return elenco;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
