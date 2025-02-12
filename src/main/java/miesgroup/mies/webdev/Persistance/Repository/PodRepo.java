package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Model.Pod;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PodRepo implements PanacheRepositoryBase<Pod, String> {

    private final DataSource dataSources;
    private final ClienteRepo clienteRepo;

    public PodRepo(DataSource dataSources, ClienteRepo clienteRepo) {
        this.dataSources = dataSources;
        this.clienteRepo = clienteRepo;
    }

    public void insert(Pod newPod) {
        persist(newPod);
    }

    public List<Pod> findAll(int id_utente) {
        Cliente c = clienteRepo.findById(id_utente);
        List<Pod> elenco = listAll();
        return elenco;
    }


    public Pod cercaIdPod(String id, int idUtente) {
        return find("id = ?1 AND utente.id = ?2", id, idUtente).firstResult();
    }

    public String verificaSePodEsiste(String idPod, int idUtente) {
        Pod pod = find("id = ?1 AND utente.id = ?2", idPod, idUtente).firstResult();
        return (pod != null) ? pod.getId() : null;
    }


    public void aggiungiSedeNazione(String idPod, String sede, String nazione, int idUtente) {
        Pod pod = find("id = ?1 AND utente.id = ?2", idPod, idUtente).firstResult();
        if (pod != null) {
            pod.setSede(sede);
            pod.setNazione(nazione);
            pod.persist(); // Salva le modifiche nel database
        } else {
            throw new IllegalArgumentException("POD con ID " + idPod + " e utente " + idUtente + " non trovato.");
        }
    }


    public List<Pod> findPodByIdUser(Integer idUser) {
        return Pod.list("utente.id", idUser);
    }


    public List<PDFFile> getBollette(List<Pod> elencoPod) {
        if (elencoPod == null || elencoPod.isEmpty()) {
            return new ArrayList<>(); // Ritorna lista vuota se non ci sono POD
        }

        List<String> podIds = elencoPod.stream().map(Pod::getId).toList();
        return PDFFile.list("idPod IN ?1", podIds);
    }

}
