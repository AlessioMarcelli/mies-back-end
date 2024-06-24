package miesgroup.mies.webdev.Service;

import miesgroup.mies.webdev.Persistance.Pod;
import miesgroup.mies.webdev.Persistance.Repository.PodRepo;

import java.util.ArrayList;
import java.util.List;

public class PodService {

    private final PodRepo podRepo;

    public PodService(PodRepo podRepo) {
        this.podRepo = podRepo;
    }

    public void createPod(String id, double Tensione_Alimentezoine, double Potenza_Impegnata, double Potenza_Disponibile, int Id_utente) {
        Pod newPod = new Pod();
        newPod.setId(id);
        newPod.setTensione_Alimentazione(Tensione_Alimentezoine);
        newPod.setPotenza_Impegnata(Potenza_Impegnata);
        newPod.setPotenza_Disponibile(Potenza_Disponibile);
        newPod.setId_utente(Id_utente);
        podRepo.insert(newPod);
    }

    public ArrayList<Pod> tutti() {
        return podRepo.findAll();
    }
}
