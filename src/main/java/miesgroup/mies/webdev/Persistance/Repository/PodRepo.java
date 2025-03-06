package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Rest.Exception.PodNotFound;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PodRepo implements PanacheRepositoryBase<Pod, String> {

    private final DataSource dataSources;
    private final ClienteRepo clienteRepo;
    private final FileRepo fileRepo;

    public PodRepo(DataSource dataSources, ClienteRepo clienteRepo, FileRepo fileRepo) {
        this.dataSources = dataSources;
        this.clienteRepo = clienteRepo;
        this.fileRepo = fileRepo;
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
        return list("utente.id", idUser);
    }


    public List<PDFFile> getBollette(List<Pod> elencoPod) {
        if (elencoPod == null || elencoPod.isEmpty()) {
            return new ArrayList<>(); // Ritorna lista vuota se non ci sono POD
        }

        List<String> podIds = elencoPod.stream().map(Pod::getId).toList();
        return fileRepo.list("idPod IN ?1", podIds);
    }

    public void aggiungiSpread(String idPod, Double spread) {
        Pod pod = findById(idPod);
        if (pod != null) {
            update("spread = ?1 WHERE id = ?2", spread, idPod);

        } else {
            throw new PodNotFound("POD con ID " + idPod + " non trovato.");
        }
    }

    public void modificaSedeNazione(String idPod, String sede, String nazione) {
        Pod pod = findById(idPod);
        if (pod != null) {
            update("sede = ?1, nazione = ?2 WHERE id = ?3", sede, nazione, idPod);
        } else {
            throw new PodNotFound("POD con ID " + idPod + " non trovato.");
        }
    }
}
