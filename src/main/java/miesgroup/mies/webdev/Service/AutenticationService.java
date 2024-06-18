package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Repository.ClienteRepo;
import miesgroup.mies.webdev.Persistance.Repository.SessionRepo;
import miesgroup.mies.webdev.Service.Exception.ClienteCreationException;
import miesgroup.mies.webdev.Service.Exception.SessionCreationException;
import miesgroup.mies.webdev.Service.Exception.WrongUsernameOrPasswordException;

import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class AutenticationService {
    private final ClienteRepo clienteRepo;
    private final HashCalculator hashCalculator;
    private final SessionRepo sessionRepo;

    public AutenticationService(ClienteRepo clienteRepo, HashCalculator hashCalculator, SessionRepo sessionRepo) {
        this.clienteRepo = clienteRepo;
        this.hashCalculator = hashCalculator;
        this.sessionRepo = sessionRepo;
    }

    public void register(String username, String password, String sedelegale, String pIva, String email, String telefono, String stato) throws ClienteCreationException {
        if (clienteRepo.existsByUsername(username)) {
            throw new ClienteCreationException();
        }
        String hash = hashCalculator.calculateHash(password);
        Cliente nuovoCliente = new Cliente();
        nuovoCliente.setUsername(username);
        nuovoCliente.setPassword(hash);
        nuovoCliente.setEmail(email);
        nuovoCliente.setpIva(pIva);
        nuovoCliente.setSedeLegale(sedelegale);
        nuovoCliente.setTelefono(telefono);
        nuovoCliente.setStato(stato);
        clienteRepo.insert(nuovoCliente);
    }

    public int login(String username, String password) throws WrongUsernameOrPasswordException, SessionCreationException {
        String hash = hashCalculator.calculateHash(password);
        Optional<Cliente> maybeCliente = clienteRepo.findByUsernamelAndPasswordHash(username, hash);
        if (maybeCliente.isPresent()) {
            Cliente c = maybeCliente.get();
            try {
                int sessione = sessionRepo.insertSession(c.getId());
                return sessione;
            } catch (SQLException e) {
                throw new SessionCreationException(e);
            }
        } else {
            throw new WrongUsernameOrPasswordException();
        }
    }

    public void logout(int sessionId) {
        sessionRepo.delete(sessionId);
    }
}
