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

    public void A2AVerificaDispacciamento(String nomeBolletta, String idPod, Double spesaMaeriaEnergia) throws SQLException {
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

    }
}
