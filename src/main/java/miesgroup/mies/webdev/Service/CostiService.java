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

    public void createCosto(String descrizione, String categoria, String unitaMisura, Integer trimestre, String anno, float valore, String tipoTensione, String classeAgevolazione) throws SQLException {
        Costi costo = new Costi();
        costo.setDescrizione(descrizione);
        costo.setCategoria(categoria);
        costo.setCosto(valore);
        costo.setUnitaMisura(unitaMisura);
        costo.setTrimestre(trimestre);
        costo.setAnno(anno);
        costo.setIntervalloPotenza(tipoTensione);
        costo.setClasseAgevolazione(classeAgevolazione);
        costiRepo.aggiungiCosto(costo);
    }


    public ArrayList<Costi> getAllCosti() {
        return costiRepo.getAllCosti();
    }

}