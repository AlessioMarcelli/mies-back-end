package miesgroup.mies.webdev.Persistance.Repository;


import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.BollettaPod;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Persistance.Model.Pod;

import java.util.List;

@ApplicationScoped
public class BollettaRepo implements PanacheRepositoryBase<BollettaPod, Integer> {

    private final CostiRepo costiRepo;
    private final PodRepo podRepo;

    public BollettaRepo(CostiRepo costiRepo, PodRepo podRepo) {
        this.costiRepo = costiRepo;
        this.podRepo = podRepo;
    }

    public Double getCorrispettiviDispacciamentoA2A(int trimestre) {
        List<Costi> lista = costiRepo.find("unitaMisura = ?1 AND categoria = 'dispacciamento' AND (trimestre = ?2 OR anno IS NOT NULL)", "€/KWh", trimestre).list();
        double somma = 0;
        for (Costi c : lista) {
            somma += c.getCosto();
        }
        return somma;
    }

    public Double getConsumoA2A(String nome) {
        BollettaPod b = find("nomeBolletta", nome).firstResult();
        return b.getF1P() + b.getF2P() + b.getF3P();
    }

    public String getTipoTensione(String idPod) {
        Pod pod = podRepo.find("id", idPod).firstResult();
        return pod.getTipoTensione();
    }

    public void updateDispacciamentoA2A(double dispacciamento, String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        b.setDispacciamento(dispacciamento);
    }

    public void updateGenerationA2A(Double generation, String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        b.setGeneration(generation);
    }

    public String getMese(String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        return bollettaPod.getMese();
    }

    public Double getPotenzaImpegnata(String idPod) {
        Pod pod = podRepo.find("id", idPod).firstResult();
        return pod.getPotenzaImpegnata();
    }

    public Double getCostiTrasporto(int trimestre, String intervalloPotenza, String unitaMisura) {
        return costiRepo.findByCategoriaUnitaTrimestre("trasporti", unitaMisura, intervalloPotenza, trimestre)
                .orElse(0.0);
    }

    public Double getCostiOneri(int trimestre, String intervalloPotenza, String unitaMisura, String classeAgevolazione) {
        List<Costi> lista = costiRepo.find("categoria = 'oneri' AND unitaMisura = ?1 AND intervalloPotenza = ?2 AND (trimestre = ?3 OR anno IS NOT NULL) AND classeAgevolazione = ?4",
                        unitaMisura, intervalloPotenza, trimestre, classeAgevolazione)
                .list();
        if (lista.isEmpty()) {
            return 0.0;
        }

        Double somma = 0.0;
        for (Costi c : lista) {
            somma += c.getCosto();
        }
        return somma;
    }

    public void updateTrasportiA2A(double trasporti, String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        bollettaPod.setVerificaTrasporti(trasporti);
    }

    public void updatePenali33(double penali33, String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        bollettaPod.setPenali33(penali33);
    }

    public void updatePenali75(double penali75, String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        bollettaPod.setPenali75(penali75);
    }

    public void updateVerificaOneri(Double costiOneri, String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        bollettaPod.setVerificaOnneri(costiOneri);
    }

    public Double getMaggiorePotenza(String nomeBolletta) {
        BollettaPod bolletta = find("nomeBolletta", nomeBolletta).firstResult();
        Double maggiore = Math.max(bolletta.getF1P(), Math.max(bolletta.getF2P(), bolletta.getF3P()));
        return maggiore;
    }

    public boolean A2AisPresent(String nomeBolletta, String idPod) {
        return count("nomeBolletta = ?1 AND idPod = ?2", nomeBolletta, idPod) > 0;
    }

    public void updateTOTReattiva(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        Double totReattiva = b.getF1R() + b.getF2R() + b.getF3R();
        b.setTotReattiva(totReattiva);
    }

    public void updateTOTAttiva(Double totAttiva, String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        b.setTotAttiva(totAttiva);
    }

    public Double getF1(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();

        Double f1 = b.getF1P();
        return f1;
    }

    public Double getF2(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        Double f2 = b.getF2P();
        return f2;

    }

    public Double getPenaliSotto75() {
        List<Costi> costi = costiRepo.find("categoria = 'penali' AND descrizione = '>33%&75%<'").list();
        Double somma = 0.0;
        for (Costi c : costi) {
            somma += c.getCosto();
        }
        return somma;
    }

    public Double getPenaliSopra75() {
        List<Costi> c = costiRepo.find("categoria = 'penali' AND descrizione = '>75%'")
                .list();
        Double somma = 0.0;
        for (Costi costi : c) {
            somma += costi.getCosto();
        }
        return somma;
    }

    public void updateVerificaImposte(double costiImposte, String nomeBolletta) {
        BollettaPod bollettaPod = find("nomeBolletta", nomeBolletta).firstResult();
        bollettaPod.setVerificaImposte(costiImposte);
    }

    public Double getF1R(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta)
                .firstResult();
        Double f1 = b.getF1R();
        return f1;
    }

    public Double getF2R(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta)
                .firstResult();
        Double f2 = b.getF2R();
        return f2;
    }

    public String verificaInserimento(String nomeBolletta) {
        BollettaPod b = find("nomeBolletta", nomeBolletta).firstResult();
        if (b == null) {
            return "Bolletta non presente";
        }
        return "Bolletta presente";
    }
}
