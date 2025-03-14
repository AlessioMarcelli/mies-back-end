package miesgroup.mies.webdev.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.Sessione;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@ApplicationScoped
public class SessionRepo implements PanacheRepositoryBase<Sessione, Integer> {
    private final DataSource dataSources;
    private final ClienteRepo clienteRepo;
    private final SessionRepo sessionRepo;

    public SessionRepo(DataSource dataSources, ClienteRepo clienteRepo, SessionRepo sessionRepo) {
        this.dataSources = dataSources;
        this.clienteRepo = clienteRepo;
        this.sessionRepo = sessionRepo;
    }


    public int insertSession(int idUtente) {
        Cliente cliente = clienteRepo.findById(idUtente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente con ID " + idUtente + " non trovato.");
        }

        Sessione sessione = new Sessione();
        sessione.setUtente(cliente);
        sessione.setDataSessione(new Timestamp(System.currentTimeMillis())); // Imposta la data attuale
        sessione.persist();

        return sessione.getId(); // Hibernate aggiorna automaticamente l'ID dopo il persist
    }


    public Optional<Sessione> getSessionByUserId(int userId) {
        return find("utente.id", userId).firstResultOptional();
    }


    public void delete(int sessionId) {
        delete("id", sessionId);
    }

    public Integer find(int idSessione) {
        Sessione sessione = sessionRepo.findById(idSessione);
        return (sessione != null) ? sessione.getUtente().getId() : null;
    }


    public Cliente findCategory(int idUtente) {
        return clienteRepo.findById(idUtente);
    }

    public Optional<Sessione> getSessionById(Integer sessionCookie) {
        return find("id", sessionCookie).firstResultOptional();
    }
}

