package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Repository.ClienteRepo;
import miesgroup.mies.webdev.Rest.Model.UpdateUtente;

@ApplicationScoped
public class ClienteService {
    private final ClienteRepo clienteRepo;

    public ClienteService(ClienteRepo clienteRepo) {
        this.clienteRepo = clienteRepo;
    }

    public String getClasseAgevolazione(String idPod) {
        return clienteRepo.getClasseAgevolazioneByPod(idPod);
    }

    public Cliente getCliente(int idUtente) {
        return clienteRepo.getCliente(idUtente);
    }

    public void updateUtente(int idUtente, String sedeLegale, String pIva, String telefono, String email, String stato, String classeAgevolazione) {
        clienteRepo.updateUtente(idUtente, sedeLegale, pIva, telefono, email, stato, classeAgevolazione);

    }
}
