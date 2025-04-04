package miesgroup.mies.webdev.Repository;


import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Model.Costi;

import javax.sql.DataSource;
import java.time.LocalDate;
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
                "AND (trimestre = 2 OR anno IS NOT NULL) " +
                "AND unitaMisura = '€/KWh'", intervalloPotenza)
                .project(Double.class)
                .firstResultOptional();

        if (sommaCosto.isEmpty()) {
            return null;
        }

        Costi costo = new Costi();
        costo.setCosto(sommaCosto.get());
        return costo;
    }


    public void deleteCosto(int id) {
        deleteById(id);
    }


    public boolean updateCosto(Costi c) {
        return update("descrizione = ?1, categoria = ?2, unitaMisura = ?3, " +
                        "trimestre = ?4, anno = ?5, costo = ?6, " +
                        "intervalloPotenza = ?7, classeAgevolazione = ?8 " +
                        "WHERE id = ?9",
                c.getDescrizione(), c.getCategoria(), c.getUnitaMisura(),
                c.getTrimestre(), c.getAnno(), c.getCosto(),
                c.getIntervalloPotenza(), c.getClasseAgevolazione(), c.getId()) > 0;
    }


    public Optional<Double> findByCategoriaUnitaTrimestre(String categoria, String unitaMisura, String intervalloPotenza, int trimestre, String annoBolletta) {
        List<Costi> costi = find("categoria = ?1 AND unitaMisura = ?2 AND intervalloPotenza = ?3 AND (trimestre = ?4 OR anno IS NOT NULL) AND annoRiferimento = ?5",
                categoria, unitaMisura, intervalloPotenza, trimestre, annoBolletta).list();

        if (costi.isEmpty()) {
            return Optional.empty();
        } else {
            double somma = 0;
            for (Costi c : costi) {
                somma += c.getCosto();
            }
            return Optional.of(somma);
        }

    }

    public List<Costi> getArticoli(String anno, String mese, String categoria, String rangePotenza) {
        int trimestre = switch (mese.toLowerCase()) {
            case "gennaio", "febbraio", "marzo" -> 1;
            case "aprile", "maggio", "giugno" -> 2;
            case "luglio", "agosto", "settembre" -> 3;
            default -> 4;
        };

        return find("""
                 categoria = ?1\s
                 AND annoRiferimento = ?2\s
                 AND intervalloPotenza = ?3\s
                 AND (trimestre = ?4 OR anno IS NOT NULL)
                \s""", categoria, anno, rangePotenza, trimestre).list();
    }


    public List<Costi> getArticoliDispacciamento(String anno, String mese, String categoria) {
        int trimestre = switch (mese.toLowerCase()) {
            case "gennaio", "febbraio", "marzo" -> 1;
            case "aprile", "maggio", "giugno" -> 2;
            case "luglio", "agosto", "settembre" -> 3;
            default -> 4;
        };

        return find("""
                 categoria = ?1\s
                 AND annoRiferimento = ?2\s
                 AND (trimestre = ?3 OR anno IS NOT NULL)
                \s""", categoria, anno, trimestre).list();
    }

    public PanacheQuery<Costi> getQueryAllCosti() {
        return findAll();
    }

    public long deleteIds(List<Long> ids) {
        return delete("id IN ?1", ids);
    }
}
