package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Persistance.Repository.CostiRepo;

import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
public class CostiService {
    private final CostiRepo costiRepo;

    public CostiService(CostiRepo costiRepo) {
        this.costiRepo = costiRepo;
    }

    public void createCosto(String descrizione, String categoria, String unitaMisura, Integer trimestre, String anno, double valore) throws SQLException {
        Costi costo = new Costi();
        costo.setDescrizione(descrizione);
        costo.setCategoria(categoria);
        costo.setCosto(valore);
        costo.setUnitaMisura(unitaMisura);
        if (trimestre == null) {
            costo.setAnno(anno);
            costiRepo.aggiungiCostoAnnuale(costo);
        } else {
            costo.setTrimestre(trimestre);
            costiRepo.aggiungiCostoTrimestrale(costo);
        }
    }

    public ArrayList<Costi> getAllCosti() {
        return costiRepo.getAllCosti();
    }
}
