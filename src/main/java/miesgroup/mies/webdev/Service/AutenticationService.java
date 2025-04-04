package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Repository.SessionRepo;
import miesgroup.mies.webdev.Service.Exception.ClienteCreationException;
import miesgroup.mies.webdev.Service.Exception.SessionCreationException;
import miesgroup.mies.webdev.Service.Exception.WrongUsernameOrPasswordException;

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

    @Transactional
    public void register(Cliente c) throws ClienteCreationException {
        if (clienteRepo.existsByUsername(c.getUsername())) {
            throw new ClienteCreationException();
        }
        String hash = hashCalculator.calculateHash(c.getPassword());
        Cliente nuovoCliente = new Cliente();
        nuovoCliente.setUsername(c.getUsername());
        nuovoCliente.setPassword(hash);
        nuovoCliente.setEmail(c.getEmail());
        nuovoCliente.setpIva(c.getpIva());
        nuovoCliente.setSedeLegale(c.getSedeLegale());
        nuovoCliente.setTelefono(c.getTelefono());
        nuovoCliente.setStato(c.getStato());
        nuovoCliente.setTipologia(c.getTipologia());
        clienteRepo.persist(nuovoCliente);
    }

    @Transactional
    public int login(String username, String password) throws WrongUsernameOrPasswordException, SessionCreationException {
        String hash = hashCalculator.calculateHash(password);
        Optional<Cliente> maybeCliente = clienteRepo.findByUsernamelAndPasswordHash(username, hash);
        if (maybeCliente.isPresent()) {
            Cliente c = maybeCliente.get();
            return sessionRepo.insertSession(c.getId());
        } else {
            throw new WrongUsernameOrPasswordException();
        }
    }

    @Transactional
    public void logout(int sessionId) {
        sessionRepo.delete(sessionId);
    }
}
