package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Repository.ClienteRepo;
import miesgroup.mies.webdev.Rest.Model.UpdateUtente;

@ApplicationScoped
public class ClienteService {
    private final ClienteRepo clienteRepo;
    private final HashCalculator hashCalculator;

    public ClienteService(ClienteRepo clienteRepo, HashCalculator hashCalculator) {
        this.clienteRepo = clienteRepo;
        this.hashCalculator = hashCalculator;
    }

    public String getClasseAgevolazione(String idPod) {
        return clienteRepo.getClasseAgevolazioneByPod(idPod);
    }

    public Cliente getCliente(int idUtente) {
        return clienteRepo.getCliente(idUtente);
    }


    public void updateCliente(int idUtente, String field, String newValue) {
        if (field.equals("password")) {
            newValue = hashCalculator.calculateHash(newValue);
        }
        clienteRepo.updateCliente(idUtente, field, newValue);
    }
}
