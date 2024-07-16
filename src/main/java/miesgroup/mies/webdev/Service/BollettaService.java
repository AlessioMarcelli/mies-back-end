package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Repository.BollettaRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

@ApplicationScoped
public class BollettaService {

    private final BollettaRepo bollettaRepo;

    public BollettaService(BollettaRepo bollettaRepo) {
        this.bollettaRepo = bollettaRepo;
    }

    public void A2AVerifica(String nomeBolletta, String idPod, Double spesaMaeriaEnergia) throws SQLException {
        //Calcolo Dispacciamento e Generation
        Double totAttiva = bollettaRepo.getConsumoA2A(nomeBolletta);
        String mese = bollettaRepo.getMese(nomeBolletta);
        int trimestre;
        if (mese.equals("gennaio") || mese.equals("febbraio") || mese.equals("marzo")) {
            trimestre = 1;
        } else if (mese.equals("aprile") || mese.equals("maggio") || mese.equals("giugno")) {
            trimestre = 2;
        } else if (mese.equals("luglio") || mese.equals("agosto") || mese.equals("settembre")) {
            trimestre = 3;
        } else {
            trimestre = 4;
        }
        Double totCorrispettivi = bollettaRepo.getCorrispettiviDispacciamentoA2A(trimestre);
        String tipoTensione = bollettaRepo.getTipoTensione(idPod);
        Double dispacciamento = totAttiva * totCorrispettivi;
        if (tipoTensione.equals("Bassa")) {
            dispacciamento = dispacciamento * 1.1;
        } else if (tipoTensione.equals("Media")) {
            dispacciamento = dispacciamento * 1.038;
        } else {
            dispacciamento = dispacciamento * 1.02;
        }
        BigDecimal bd = new BigDecimal(dispacciamento);
        bd = bd.setScale(7, RoundingMode.HALF_UP);
        dispacciamento = bd.doubleValue();
        bollettaRepo.updateDispacciamentoA2A(dispacciamento, nomeBolletta);
        Double generation = spesaMaeriaEnergia - dispacciamento;
        bollettaRepo.updateGenerationA2A(generation, nomeBolletta);

        //Calcolo Trasporti
        Double potenzaImpegnata = bollettaRepo.getPotenzaImpegnata(idPod);
        double costi;
        if (potenzaImpegnata < 100) {
            costi = bollettaRepo.getCostiSotto100();
        } else if (potenzaImpegnata > 100 || potenzaImpegnata < 500) {
            costi = bollettaRepo.getCostiSotto500();
        } else {
            costi = bollettaRepo.getCostiSopra500();
        }

        double f1Attiva = bollettaRepo.getF1(nomeBolletta);
        double f2Attiva = bollettaRepo.getF2(nomeBolletta);
        double sommaAttiva = f1Attiva + f2Attiva;
        double f1Reattiva = bollettaRepo.getF1Reattiva(nomeBolletta);
        double f2Reattiva = bollettaRepo.getF2Reattiva(nomeBolletta);
        double sommaReattiva = f1Reattiva + f2Reattiva;

        double percentualeDelleAR = (sommaReattiva / sommaAttiva) * 100;

        double noPenali = 0;
        double costo3375 = 0;
        double costo75 = 0;
        double penali33 = 0;
        double penali75 = 0;
        double percentualeDelleAR3375 = 0;
        double percentualeDelleAR75 = 0;
        double trasporti = 0;
        if (percentualeDelleAR < 33) {
            noPenali = 0;
        } else if (percentualeDelleAR > 33 || percentualeDelleAR < 75) {
            percentualeDelleAR3375 = (percentualeDelleAR - 33) - (percentualeDelleAR - 75);
            costo3375 = bollettaRepo.getPenaliSotto75();
            penali33 = (costo3375 * sommaAttiva) * (percentualeDelleAR3375 / 100);
            trasporti = costi + penali33;
        } else {
            percentualeDelleAR3375 = (percentualeDelleAR - 33) - (percentualeDelleAR - 75);
            costo3375 = bollettaRepo.getPenaliSotto75();
            penali33 = (costo3375 * sommaAttiva) * (percentualeDelleAR3375 / 100);
            percentualeDelleAR75 = percentualeDelleAR - 75;
            costo75 = bollettaRepo.getPenaliSopra75();
            penali75 = (costo75 * sommaAttiva) * (percentualeDelleAR75 / 100);
            trasporti = costi + penali33 + penali75;
        }

        bollettaRepo.updatePenali33(penali33, nomeBolletta);
        bollettaRepo.updatePenali75(penali75, nomeBolletta);
        bollettaRepo.updateTrasportiA2A(trasporti, nomeBolletta);

    }
}
