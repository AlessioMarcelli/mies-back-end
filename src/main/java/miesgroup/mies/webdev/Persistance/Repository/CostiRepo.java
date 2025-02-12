package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Costi;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CostiRepo implements PanacheRepositoryBase<Costi, Integer> {
    private final DataSource dataSource;

    public CostiRepo(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public boolean aggiungiCosto(Costi costo) {
        costo.persist();
        return costo.isPersistent(); // Restituisce true se l'entità è stata salvata correttamente
    }


    public List<Costi> getAllCosti() {
        return listAll();
    }


    public Costi getSum(String intervalloPotenza) {
        Optional<Double> sommaCosto = find("intervalloPotenza = ?1 AND categoria = 'trasporti' " +
                "AND (trimestrale = 2 OR annuale IS NOT NULL) " +
                "AND unitaMisura = '€/KWh'", intervalloPotenza)
                .project(Double.class)
                .firstResultOptional();

        if (sommaCosto.isEmpty()) {
            return null;
        }

        Costi costo = new Costi();
        costo.setCosto(sommaCosto.get().floatValue());
        return costo;
    }


    public void deleteCosto(int id) {
        deleteById(id);
    }


    public boolean updateCosto(Costi c) {
        return update("descrizione = ?1, categoria = ?2, unitaMisura = ?3, " +
                        "trimestrale = ?4, annuale = ?5, costo = ?6, " +
                        "intervalloPotenza = ?7, classeAgevolazione = ?8 " +
                        "WHERE id = ?9",
                c.getDescrizione(), c.getCategoria(), c.getUnitaMisura(),
                c.getTrimestre(), c.getAnno(), c.getCosto(),
                c.getIntervalloPotenza(), c.getClasseAgevolazione(), c.getId()) > 0;
    }


    public void save(Costi dettaglioCosto) {
        dettaglioCosto.persist();
    }


    public Optional<Double> findByCategoriaUnitaTrimestre(String categoria, String unitaMisura, String intervalloPotenza, int trimestre) {
        return find("unitàMisura = ?1 AND categoria = ?2 AND intervalloPotenza = ?3 AND (trimestrale = ?4 OR annuale IS NOT NULL)",
                unitaMisura, categoria, intervalloPotenza, trimestre)
                .project(Double.class)
                .firstResultOptional();
    }
}
