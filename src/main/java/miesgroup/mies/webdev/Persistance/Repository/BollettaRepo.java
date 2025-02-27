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

    public Double getCorrispettiviDispacciamentoA2A(int trimestre, String annoRiferimento) {
        List<Costi> lista = costiRepo.find("unitaMisura = ?1 AND categoria = 'dispacciamento' AND (trimestre = ?2 OR anno IS NOT NULL) AND annoRiferimento = ?3", "€/KWh", trimestre, annoRiferimento).list();
        double somma = 0;
        for (Costi c : lista) {
            somma += c.getCosto();
        }
        return somma;
    }

    public Double getConsumoA2A(String nome, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nome, mese).firstResult();
        return b.getTotAttiva();
    }

    public String getTipoTensione(String idPod) {
        Pod pod = podRepo.find("id", idPod).firstResult();
        return pod.getTipoTensione();
    }

    public void updateDispacciamentoA2A(double dispacciamento, String nome, String mese) {
        update("SET dispacciamento = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", dispacciamento, nome, mese);
    }


    public void updateGenerationA2A(Double generation, String nome, String mese) {
        update("SET generation = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", generation, nome, mese);
    }


    public Double getPotenzaImpegnata(String idPod) {
        Pod pod = podRepo.find("id", idPod).firstResult();
        return pod.getPotenzaImpegnata();
    }

    public Double getCostiTrasporto(int trimestre, String intervalloPotenza, String unitaMisura, String annoBolletta) {
        return costiRepo.findByCategoriaUnitaTrimestre("trasporti", unitaMisura, intervalloPotenza, trimestre, annoBolletta)
                .orElse(0.0);
    }

    public Double getCostiOneri(int trimestre, String intervalloPotenza, String unitaMisura, String classeAgevolazione, String annoRiferimento) {
        List<Costi> lista = costiRepo.find("categoria = 'oneri' AND unitaMisura = ?1 AND intervalloPotenza = ?2 AND (trimestre = ?3 OR anno IS NOT NULL) AND classeAgevolazione = ?4 AND annoRiferimento = ?5",
                unitaMisura, intervalloPotenza, trimestre, classeAgevolazione, annoRiferimento).list();
        if (lista.isEmpty()) {
            return 0.0;
        }

        Double somma = 0.0;
        for (Costi c : lista) {
            somma += c.getCosto();
        }
        return somma;
    }

    public void updateVerificaTrasportiA2A(double trasporti, String nomeBolletta, String mese) {
        update("SET verificaTrasporti = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", trasporti, nomeBolletta, mese);
    }


    public void updatePenali33(double penali33, String nomeBolletta, String mese) {
        update("SET penali33 = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", penali33, nomeBolletta, mese);
    }


    public void updatePenali75(double penali75, String nomeBolletta, String mese) {
        update("SET penali75 = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", penali75, nomeBolletta, mese);
    }


    public void updateVerificaOneri(Double costiOneri, String nomeBolletta, String mese) {
        update("SET verificaOneri = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", costiOneri, nomeBolletta, mese);
    }


    public Double getMaggiorePotenza(String nomeBolletta) {
        BollettaPod bolletta = find("nomeBolletta", nomeBolletta).firstResult();
        Double maggiore = Math.max(bolletta.getF1P(), Math.max(bolletta.getF2P(), bolletta.getF3P()));
        return maggiore;
    }

    public boolean A2AisPresent(String nomeBolletta, String idPod) {
        return count("nomeBolletta = ?1 AND idPod = ?2", nomeBolletta, idPod) > 0;
    }

    public Double getF1(String nomeBolletta, String mese) {

        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();

        Double f1 = b.getF1P();
        return f1;
    }

    public Double getF2(String nomeBolletta, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();
        Double f2 = b.getF2P();
        return f2;

    }

    public Double getPenaliSotto75(String annoRiferimento) {
        List<Costi> costi = costiRepo.find("categoria = 'penali' AND descrizione = '>33%&75%<' AND annoRiferimento = ?1", annoRiferimento).list();
        Double somma = 0.0;
        for (Costi c : costi) {
            somma += c.getCosto();
        }
        return somma;
    }

    public Double getPenaliSopra75(String annoRiferiemnto) {
        List<Costi> c = costiRepo.find("categoria = 'penali' AND descrizione = '>75%' AND annoRiferimento = ?1", annoRiferiemnto).list();
        Double somma = 0.0;
        for (Costi costi : c) {
            somma += costi.getCosto();
        }
        return somma;
    }

    public void updateVerificaImposte(double costiImposte, String nomeBolletta, String mese) {
        update("SET verificaImposte = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", costiImposte, nomeBolletta, mese);
    }


    public Double getF1R(String nomeBolletta, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();
        Double f1 = b.getF1R();
        return f1;
    }

    public Double getF2R(String nomeBolletta, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();
        Double f2 = b.getF2R();
        return f2;
    }

    public Double getPiccoKwh(String nomeBolletta, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();
        return b.getPiccoKwh();
    }

    public Double getFuoriPiccoKwh(String nomeBolletta, String mese) {
        BollettaPod b = find("nomeBolletta = ?1 AND mese = ?2", nomeBolletta, mese).firstResult();
        return b.getFuoriPiccoKwh();
    }

    public Double getCostoFuoriPicco(int trimestre, String annoBolletta, String intervalloPotenza) {
        List<Costi> furoiPicco = costiRepo.find("categoria = 'fuori picco' AND ( trimestre = ?1 OR anno IS NOT NULL ) AND annoRiferimento = ?2 AND intervalloPotenza = ?3",
                trimestre, annoBolletta, intervalloPotenza).list();

        if (furoiPicco.isEmpty()) {
            return 0.0;
        }

        double somma = 0;
        for (Costi c : furoiPicco) {
            somma += c.getCosto();
        }
        return somma;
    }

    public Double getCostoPicco(int trimestre, String anno, String rangePotenza) {
        List<Costi> picco = costiRepo.find("categoria = 'picco' AND ( trimestre = ?1 OR anno IS NOT NULL ) AND annoRiferimento = ?2 AND intervalloPotenza = ?3",
                trimestre, anno, rangePotenza).list();

        if (picco.isEmpty()) {
            return 0.0;
        }

        double somma = 0;
        for (Costi c : picco) {
            somma += c.getCosto();
        }
        return somma;
    }

    public void updateVerificaPicco(Double verificaPicco, String nomeBolletta, String mese) {
        update("SET verificaPicco = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", verificaPicco, nomeBolletta, mese);
    }


    public void updateVerificaFuoriPicco(Double verificaFuoriPicco, String nomeBolletta, String mese) {
        update("SET verificaFuoriPicco = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", verificaFuoriPicco, nomeBolletta, mese);
    }


    public void updateTOTAttivaPerdite(Double totAttivaPerdite, String nomeBolletta, String mese) {
        update("SET totAttivaPerdite = ?1 WHERE nomeBolletta = ?2 AND mese = ?3", totAttivaPerdite, nomeBolletta, mese);
    }

}
