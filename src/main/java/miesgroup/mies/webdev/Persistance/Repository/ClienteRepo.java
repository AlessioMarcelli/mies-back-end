package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Service.LoggerService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;


@ApplicationScoped
public class ClienteRepo implements PanacheRepositoryBase<Cliente, Integer> {
    private final DataSource dataSource;
    private final LoggerService loggerService;

    public ClienteRepo(DataSource dataSources, LoggerService loggerService) {
        this.dataSource = dataSources;
        this.loggerService = loggerService;
    }


    public boolean existsByUsername(String username) {
        return count("Username", username) > 0;
    }

    public void insert(Cliente nuovoCliente) {
        nuovoCliente.persist();
    }

    public Optional<Cliente> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public Optional<Cliente> findByUsernamelAndPasswordHash(String username, String password) {
        return find("username = ?1 and password = ?2", username, password).firstResultOptional();
    }

    public String getClasseAgevolazioneByPod(String idPod) {
        return Pod.find("id", idPod)
                .project(Pod.class)
                .firstResultOptional()
                .flatMap(pod -> Cliente.find("id", pod.getUtente().getId())
                        .project(Cliente.class)
                        .firstResultOptional()
                        .map(Cliente::getClasseAgevolazione))
                .orElse(null);
    }


    public Cliente getCliente(Integer idUtente) {
        return findById(idUtente);
    }


    //TODO: Cambiare il metodo in un mero update generico
    public boolean updateCliente(int idUtente, String field, String newValue) {
        Cliente cliente = findById(idUtente);
        if (cliente == null) {
            return false;
        }

        switch (field) {
            case "username":
                cliente.setUsername(newValue);
                break;
            case "password":
                cliente.setPassword(newValue);
                break;
            case "sedeLegale":
                cliente.setSedeLegale(newValue);
                break;
            case "pIva":
                cliente.setpIva(newValue);
                break;
            case "stato":
                cliente.setStato(newValue);
                break;
            case "email":
                cliente.setEmail(newValue);
                break;
            case "telefono":
                cliente.setTelefono(newValue);
                break;
            case "tipologia":
                cliente.setTipologia(newValue);
                break;
            case "classeAgevolazione":
                cliente.setClasseAgevolazione(newValue);
                break;
            case "codiceAteco":
                cliente.setCodiceAteco(newValue);
                break;
            case "energivori":
                cliente.setEnergivori(Boolean.parseBoolean(newValue));
                break;
            case "gassivori":
                cliente.setGassivori(Boolean.parseBoolean(newValue));
                break;
            case "consumoAnnuoEnergia":
                try {
                    cliente.setConsumoAnnuoEnergia(Float.parseFloat(newValue));
                } catch (NumberFormatException e) {
                    return false; // Fallisce se il valore non è un numero valido
                }
                break;
            case "fatturatoAnnuo":
                try {
                    cliente.setFatturatoAnnuo(Float.parseFloat(newValue));
                } catch (NumberFormatException e) {
                    return false;
                }
                break;
            default:
                return false; // Campo non valido
        }
        return true;
    }


}
