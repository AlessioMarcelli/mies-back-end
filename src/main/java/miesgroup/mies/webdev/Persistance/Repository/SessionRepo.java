package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.Sessione;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@ApplicationScoped
public class SessionRepo implements PanacheRepositoryBase<Sessione, Integer> {
    private final DataSource dataSources;

    public SessionRepo(DataSource dataSources) {
        this.dataSources = dataSources;
    }


    public int insertSession(int idUtente) {
        Cliente cliente = Cliente.findById(idUtente);
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
        return Sessione.find("utente.id", userId).firstResultOptional();
    }


    public void delete(int sessionId) {
        delete("id", sessionId);
    }

    public Integer find(int idSessione) {
        Sessione sessione = Sessione.findById(idSessione);
        return (sessione != null) ? sessione.getUtente().getId() : null;
    }


    public Cliente findCategory(int sessionId) {
        return Cliente.find("id", sessionId).project(Cliente.class).firstResult();
    }

}

