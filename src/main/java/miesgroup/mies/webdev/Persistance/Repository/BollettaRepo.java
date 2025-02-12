package miesgroup.mies.webdev.Persistance.Repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.BollettaPod;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Persistance.Model.Pod;

@ApplicationScoped
public class BollettaRepo implements PanacheRepositoryBase<BollettaPod, Integer> {

    private final BollettaRepo bollettaRepo;
    private final CostiRepo costiRepo;
    private final PodRepo podRepo;

    public BollettaRepo(BollettaRepo bollettaRepo, CostiRepo costiRepo, PodRepo podRepo) {
        this.bollettaRepo = bollettaRepo;
        this.costiRepo = costiRepo;
        this.podRepo = podRepo;
    }


    public Double getCorrispettiviDispacciamentoA2A(int trimestre) {
        return Costi.find("unitàMisura = ?1 AND categoria = 'dispacciamento' AND (trimestrale = ?2 OR annuale IS NOT NULL)", "€/KWh", trimestre)
                .project(Double.class)
                .firstResultOptional()
                .orElse(null);
    }

    public Double getConsumoA2A(String nome) {
        return BollettaPod.find("nomeBolletta", nome)
                .project(Double.class)
                .firstResultOptional()
                .orElse(null);
    }

    public String getTipoTensione(String idPod) {
        return Pod.find("id", idPod)
                .project(String.class)
                .firstResultOptional()
                .orElse(null);
    }

    public void updateDispacciamentoA2A(double dispacciamento, String nomeBolletta) {
        update("dispacciamento = ?1 WHERE nomeBolletta = ?2", dispacciamento, nomeBolletta);
    }

    public void updateGenerationA2A(Double generation, String nomeBolletta) {
        update("generation = ?1 WHERE nomeBolletta = ?2", generation, nomeBolletta);
    }

    public void updateMeseBolletta(String mese, String nomeBolletta) {
        update("mese = ?1 WHERE nomeBolletta = ?2", mese, nomeBolletta);
    }

    public String getMese(String nomeBolletta) {
        return find("nomeBolletta", nomeBolletta)
                .project(String.class)
                .firstResult();
    }

    public Double getPotenzaImpegnata(String idPod) {
        return Pod.find("id", idPod)
                .project(Double.class)
                .firstResultOptional()
                .orElse(null);
    }

    public Double getCostiTrasporto(int trimestre, String intervalloPotenza, String unitaMisura) {
        return costiRepo.findByCategoriaUnitaTrimestre("trasporti", unitaMisura, intervalloPotenza, trimestre)
                .orElse(0.0);
    }

    public Double getCostiOneri(int trimestre, String intervalloPotenza, String unitaMisura, String classeAgevolazione) {
        return Costi.find("categoria = 'oneri' AND unitàMisura = ?1 AND intervalloPotenza = ?2 AND (trimestrale = ?3 OR annuale IS NOT NULL) AND classeAgevolazione = ?4",
                        unitaMisura, intervalloPotenza, trimestre, classeAgevolazione)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }


    public Double getPenali(String descrizione) {
        return Costi.find("categoria = 'penali' AND descrizione = ?1", descrizione)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public void updateTrasportiA2A(double trasporti, String nomeBolletta) {
        update("verificaTrasporti = ?1 WHERE nomeBolletta = ?2", trasporti, nomeBolletta);
    }

    public void updatePenali(double penali, String nomeBolletta, boolean sopra75) {
        String column = sopra75 ? "penali75" : "penali33";
        update(column + " = ?1 WHERE nomeBolletta = ?2", penali, nomeBolletta);
    }

    public Double getMaggiorePotenza(String nomeBolletta) {
        return find("SELECT GREATEST(f1Potenza, f2Potenza, f3Potenza) FROM BollettaPod WHERE nomeBolletta = ?1", nomeBolletta)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public boolean A2AisPresent(String nomeBolletta, String idPod) {
        return count("nomeBolletta = ?1 AND idPod = ?2", nomeBolletta, idPod) > 0;
    }

    public void updateTOTReattiva(String nomeBolletta) {
        update("totReattiva = reattiva1 + reattiva2 + reattiva3 WHERE nomeBolletta = ?1", nomeBolletta);
    }

    public void updateTOTAttiva(Double totAttiva, String nomeBolletta) {
        update("totAttiva = ?1 WHERE nomeBolletta = ?2", totAttiva, nomeBolletta);
    }


    public double getF1(String nomeBolletta) {
        return find("SELECT f1Potenza FROM BollettaPod WHERE nomeBolletta = ?1", nomeBolletta)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }


    public double getF2(String nomeBolletta) {
        return find("SELECT f2Potenza FROM BollettaPod WHERE nomeBolletta = ?1", nomeBolletta)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public double getF1Reattiva(String nomeBolletta) {
        return find("SELECT f1Reattiva FROM BollettaPod WHERE nomeBolletta = ?1", nomeBolletta)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public double getF2Reattiva(String nomeBolletta) {
        return find("SELECT f2Reattiva FROM BollettaPod WHERE nomeBolletta = ?1", nomeBolletta)
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public double getPenaliSotto75() {
        return Costi.find("categoria = 'penali' AND descrizione = '>33%&75%<'")
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public double getPenaliSopra75() {
        return Costi.find("categoria = 'penali' AND descrizione = '>75%'")
                .project(Double.class)
                .firstResultOptional()
                .orElse(0.0);
    }

    public void updatePenali33(double penali33, String nomeBolletta) {
        update("penali33 = ?1 WHERE nomeBolletta = ?2", penali33, nomeBolletta);
    }

    public void updatePenali75(double penali75, String nomeBolletta) {
        update("penali75 = ?1 WHERE nomeBolletta = ?2", penali75, nomeBolletta);
    }

    public void updateVerificaOneri(double costiOneri, String nomeBolletta) {
        update("verificaOneri = ?1 WHERE nomeBolletta = ?2", costiOneri, nomeBolletta);
    }

    public void updateVerificaImposte(double costiImposte, String nomeBolletta) {
        update("verificaImposte = ?1 WHERE nomeBolletta = ?2", costiImposte, nomeBolletta);
    }


}