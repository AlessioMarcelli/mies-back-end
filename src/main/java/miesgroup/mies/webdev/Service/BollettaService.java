package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Persistance.Model.BollettaPod;
import miesgroup.mies.webdev.Persistance.Repository.BollettaRepo;
import miesgroup.mies.webdev.Persistance.Repository.CostiRepo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@ApplicationScoped
public class BollettaService {

    private final BollettaRepo bollettaRepo;
    private final ClienteService clienteService;


    public BollettaService(BollettaRepo bollettaRepo, ClienteService clienteService) {
        this.bollettaRepo = bollettaRepo;
        this.clienteService = clienteService;
    }

    @Transactional
    public void A2AVerifica(BollettaPod b) {

        try {

            // Recupera e controlla TOT Attiva
            double totAttiva = Optional.ofNullable(bollettaRepo.getConsumoA2A(b.getNomeBolletta(), b.getMese()))
                    .orElse(0.0);

            if (totAttiva <= 0) {
                System.err.println("Errore: TOT_Attiva non valido per " + b.getNomeBolletta());
                return;
            }

            // Recupera e controlla Potenza Impegnata
            double potenzaImpegnata = Optional.ofNullable(bollettaRepo.getPotenzaImpegnata(b.getIdPod()))
                    .orElse(0.0);
            if (potenzaImpegnata <= 0) {
                System.err.println("Errore: Potenza Impegnata non trovata per " + b.getNomeBolletta());
                return;
            }

            // Calcolo TOT Attiva Perdite
            Double totAttivaPerdite = (potenzaImpegnata < 100) ? totAttiva :
                    (potenzaImpegnata <= 500) ? totAttiva * 1.1 :
                            totAttiva * 1.038;

            bollettaRepo.updateTOTAttivaPerdite(totAttivaPerdite, b.getNomeBolletta(), b.getMese());

            // Determinare il trimestre
            int trimestre = switch (b.getMese().toLowerCase()) {
                case "gennaio", "febbraio", "marzo" -> 1;
                case "aprile", "maggio", "giugno" -> 2;
                case "luglio", "agosto", "settembre" -> 3;
                default -> 4;
            };

            // Recupera Corrispettivi Dispacciamento
            Double totCorrispettivi = Optional.ofNullable(bollettaRepo.getCorrispettiviDispacciamentoA2A(trimestre, b.getAnno()))
                    .orElse(0.0);
            String tipoTensione = bollettaRepo.getTipoTensione(b.getIdPod());

            // Calcolo Dispacciamento
            Double dispacciamento = totAttiva * totCorrispettivi;
            dispacciamento *= switch (tipoTensione) {
                case "Bassa" -> 1.1;
                case "Media" -> 1.038;
                default -> 1.02;
            };

            bollettaRepo.updateDispacciamentoA2A(arrotonda(dispacciamento), b.getNomeBolletta(), b.getMese());

            // Calcolo Generation
            Double generation = arrotonda(b.getSpeseEnergia() - dispacciamento);
            bollettaRepo.updateGenerationA2A(generation, b.getNomeBolletta(), b.getMese());

            // Calcolo Trasporti
            Double maggiorePotenza = Optional.ofNullable(bollettaRepo.getMaggiorePotenza(b.getNomeBolletta()))
                    .orElse(0.0);
            String rangePotenza = (potenzaImpegnata <= 100) ? "<100KW" :
                    (potenzaImpegnata <= 500) ? "100-500KW" : ">500KW";

            Double quotaVariabileT = Optional.ofNullable(bollettaRepo.getCostiTrasporto(trimestre, rangePotenza, "€/KWh", b.getAnno()))
                    .orElse(0.0);
            Double quotaFissaT = Optional.ofNullable(bollettaRepo.getCostiTrasporto(trimestre, rangePotenza, "€/Month", b.getAnno()))
                    .orElse(0.0);
            Double quotaPotenzaT = Optional.ofNullable(bollettaRepo.getCostiTrasporto(trimestre, rangePotenza, "€/KW/Month", b.getAnno()))
                    .orElse(0.0) * maggiorePotenza;

            bollettaRepo.updateQuoteTrasporto(quotaVariabileT, quotaFissaT, quotaPotenzaT, b.getNomeBolletta(), b.getMese());

            double costiTrasporti = arrotonda((quotaVariabileT * totAttiva) + quotaFissaT + quotaPotenzaT);
            bollettaRepo.updateVerificaTrasportiA2A(costiTrasporti, b.getNomeBolletta(), b.getMese());

            // Calcolo Imposte
            double costiImposte = (totAttiva <= 200000) ? totAttiva * 0.0125 :
                    (totAttiva <= 1200000) ? (200000 * 0.0125) + ((totAttiva - 200000) * 0.0075) :
                            (200000 * 0.0125) + (1000000 * 0.0075);

            bollettaRepo.updateVerificaImposte(arrotonda(costiImposte), b.getNomeBolletta(), b.getMese());

            // Calcolo degli Oneri
            String classeAgevolazione = clienteService.getClasseAgevolazione(b.getIdPod());

            Double quotaEnergiaOneri = Optional.ofNullable(bollettaRepo.getCostiOneri(trimestre, rangePotenza, "€/KWh", classeAgevolazione, b.getAnno()))
                    .orElse(0.0);
            Double quotaFissaOneri = Optional.ofNullable(bollettaRepo.getCostiOneri(trimestre, rangePotenza, "€/Month", classeAgevolazione, b.getAnno()))
                    .orElse(0.0);
            Double quotaPotenzaOneri = Optional.ofNullable(bollettaRepo.getCostiOneri(trimestre, rangePotenza, "€/KW/Month", classeAgevolazione, b.getAnno()))
                    .orElse(0.0) * maggiorePotenza;

            bollettaRepo.updateQuoteOneri(quotaEnergiaOneri, quotaFissaOneri, quotaPotenzaOneri, b.getNomeBolletta(), b.getMese());

            Double costiOneri = arrotonda((quotaEnergiaOneri * totAttiva) + quotaFissaOneri + quotaPotenzaOneri);
            bollettaRepo.updateVerificaOneri(costiOneri, b.getNomeBolletta(), b.getMese());


            // Calcolo delle Penali
            Double f1Attiva = Optional.ofNullable(bollettaRepo.getF1(b.getNomeBolletta(), b.getMese())).orElse(0.0);
            Double f2Attiva = Optional.ofNullable(bollettaRepo.getF2(b.getNomeBolletta(), b.getMese())).orElse(0.0);
            Double f1Reattiva = Optional.ofNullable(bollettaRepo.getF1R(b.getNomeBolletta(), b.getMese())).orElse(0.0);
            Double f2Reattiva = Optional.ofNullable(bollettaRepo.getF2R(b.getNomeBolletta(), b.getMese())).orElse(0.0);

            Double sommaAttiva = f1Attiva + f2Attiva;
            Double sommaReattiva = f1Reattiva + f2Reattiva;

            Double percentualeDelleAR = (sommaReattiva / sommaAttiva) * 100;

            Double penali33 = 0.0;
            Double penali75 = 0.0;
            if (percentualeDelleAR >= 33) {
                Double costo33 = Optional.ofNullable(bollettaRepo.getPenaliSotto75(b.getAnno())).orElse(0.0);
                Double costo75 = Optional.ofNullable(bollettaRepo.getPenaliSopra75(b.getAnno())).orElse(0.0);
                if (percentualeDelleAR < 75) {
                    penali33 = (costo33 * sommaAttiva) * ((percentualeDelleAR - 33) / 100);
                } else {
                    penali33 = (costo33 * sommaAttiva) * ((percentualeDelleAR - 33) / 100);
                    penali75 = (costo75 * sommaAttiva) * ((percentualeDelleAR - 75) / 100);
                }
            }

            bollettaRepo.updatePenali33(arrotonda(penali33), b.getNomeBolletta(), b.getMese());
            bollettaRepo.updatePenali75(arrotonda(penali75), b.getNomeBolletta(), b.getMese());

            // Calcolo Verifica Picco e Fuori Picco
            Double piccoKwh = Optional.ofNullable(bollettaRepo.getPiccoKwh(b.getNomeBolletta(), b.getMese()))
                    .orElse(0.0);
            Double fuoriPiccoKwh = Optional.ofNullable(bollettaRepo.getFuoriPiccoKwh(b.getNomeBolletta(), b.getMese()))
                    .orElse(0.0);

            Double costoPicco = Optional.ofNullable(bollettaRepo.getCostoPicco(trimestre, b.getAnno(), rangePotenza))
                    .orElse(0.0);
            Double costoFuoriPicco = Optional.ofNullable(bollettaRepo.getCostoFuoriPicco(trimestre, b.getAnno(), rangePotenza))
                    .orElse(0.0);

            bollettaRepo.updateVerificaPicco(arrotonda(piccoKwh * costoPicco), b.getNomeBolletta(), b.getMese());
            bollettaRepo.updateVerificaFuoriPicco(arrotonda(fuoriPiccoKwh * costoFuoriPicco), b.getNomeBolletta(), b.getMese());
        } catch (Exception e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    /**
     * Arrotonda un valore a 8 decimali
     */
    public static double arrotonda(double valore) {
        return BigDecimal.valueOf(valore).setScale(8, RoundingMode.HALF_UP).doubleValue();
    }


    @Transactional
    public boolean A2AisPresent(String nomeBolletta, String idPod) {
        return bollettaRepo.A2AisPresent(nomeBolletta, idPod);
    }
}
