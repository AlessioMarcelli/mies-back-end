package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.AlertData;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Rest.Model.ClienteResponse;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ClienteService {
    private final ClienteRepo clienteRepo;
    private final HashCalculator hashCalculator;

    public ClienteService(ClienteRepo clienteRepo, HashCalculator hashCalculator) {
        this.clienteRepo = clienteRepo;
        this.hashCalculator = hashCalculator;
    }

    @Transactional
    public String getClasseAgevolazione(String idPod) {
        return clienteRepo.getClasseAgevolazioneByPod(idPod);
    }

    @Transactional
    public Cliente getCliente(int idUtente) {
        return clienteRepo.getCliente(idUtente);
    }

    @Transactional
    public boolean updateCliente(int idUtente, String field, String newValue) {
        if (field.equals("password")) {
            newValue = hashCalculator.calculateHash(newValue);
        }
        return clienteRepo.updateCliente(idUtente, field, newValue);
    }

    public ClienteResponse parseResponse(Cliente c) {
        return new ClienteResponse(c.getUsername(), c.getEmail(), c.getpIva(), c.getSedeLegale(), c.getTelefono(), c.getStato(), c.getTipologia());
    }

    public AlertData[] checkUserAlertFillField(int idUtente) {
        return clienteRepo.checkUserAlertFillField(idUtente);
    }

    public Boolean getCheckEmailStatus(int idUtente) {
        return clienteRepo.getCheckEmailStatus(idUtente);
    }

    public Map<String, Boolean> checkAlertStates(int idUtente) {
        return clienteRepo.checkAlertStates(idUtente);
    }

    public Map<String, Boolean> deleteUserAlert(int idUtente, String futuresType) {
        return clienteRepo.deleteUserAlert(idUtente, futuresType);
    }

    public boolean updateDataFuturesAlert(int idUtente, String futuresType, double[] maxPriceValue, double[] minPriceValue, String[] frequency, boolean[] checkModality, boolean checkEmail) {
        return clienteRepo.updateDataFuturesAlert(idUtente, futuresType, maxPriceValue, minPriceValue, frequency, checkModality, checkEmail);
    }

    public List<Cliente> getClientsCheckEmail() {
        return clienteRepo.getClientsCheckEmail();
    }

    public Cliente getClienteByPod(String idPod) {
        return clienteRepo.getClienteByPod(idPod);
    }
}
