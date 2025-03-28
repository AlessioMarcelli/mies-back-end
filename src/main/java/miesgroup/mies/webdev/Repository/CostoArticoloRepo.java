package miesgroup.mies.webdev.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.BollettaPod;
import miesgroup.mies.webdev.Model.CostoArticolo;

import java.util.List;

@ApplicationScoped
public class CostoArticoloRepo implements PanacheRepositoryBase<CostoArticolo, Integer> {


    public void aggiungiCostoArticolo(BollettaPod b, Double costoArticolo,String nomeArticolo) {
        CostoArticolo costo = new CostoArticolo();
        costo.setNomeBolletta(b);
        costo.setCostoUnitario(costoArticolo);
        costo.setNomeArticolo(nomeArticolo);
        costo.setMese(b.getMese());
        costo.persist();
    }

    public List<CostoArticolo> getCostiArticoli(List<BollettaPod> bollettePods) {
        return list("nomeBolletta", bollettePods);
    }
}
