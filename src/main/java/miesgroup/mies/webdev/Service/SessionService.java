package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Repository.SessionRepo;
@ApplicationScoped
public class SessionService {

    private final SessionRepo sessionRepo;

    public SessionService(SessionRepo sessionRepo) {
        this.sessionRepo = sessionRepo;
    }

    public Integer trovaUtentebBySessione (int id_sessione){
        return sessionRepo.find(id_sessione);
    }

    public Cliente trovaUtenteCategoryBySessione(int sessionId) {
        int id = sessionRepo.find(sessionId);
        return sessionRepo.findCategory(id);
    }
}
