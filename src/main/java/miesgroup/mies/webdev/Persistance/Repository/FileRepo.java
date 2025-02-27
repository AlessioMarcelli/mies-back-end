package miesgroup.mies.webdev.Persistance.Repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Persistance.Model.BollettaPod;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Model.Periodo;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class FileRepo implements PanacheRepositoryBase<PDFFile, Integer> {

    private final DataSource dataSource;
    private final BollettaRepo bollettaRepo;

    public FileRepo(DataSource dataSources, BollettaRepo bollettaRepo) {
        this.dataSource = dataSources;
        this.bollettaRepo = bollettaRepo;
    }

    public int insert(PDFFile pdfFile) {
        // Check if the file name already exists
        Optional<PDFFile> existingFile = find("fileName", pdfFile.getFileName()).firstResultOptional();
        if (existingFile.isPresent()) {
            throw new RuntimeException("File name already exists in the database");
        }

        // Persist the new file
        pdfFile.persist();

        return pdfFile.getIdFile(); // Returns the auto-generated ID
    }


    public void abbinaPod(int idFile, String idPod) {
        update("idPod = ?1 WHERE idFile = ?2", idPod, idFile);
    }

    public PDFFile findById(int id) {
        return PDFFile.findById(id);
    }


    public byte[] getFile(int id) {
        PDFFile f = findById(id);
        return f.getFileData();
    }

    @Transactional
    public void saveDataToDatabase(
            Map<String, Map<String, Map<String, Integer>>> lettureMese,
            Map<String, Double> spese,
            String idPod,
            String nomeBolletta,
            Map<String, Map<String, Map<String, Double>>> piccoEFuoriPicco,
            Periodo periodo
    ) {
        for (Map.Entry<String, Map<String, Map<String, Integer>>> meseEntry : lettureMese.entrySet()) {
            String mese = meseEntry.getKey();
            Map<String, Map<String, Integer>> categorie = meseEntry.getValue();

            // Extract consumption data
            Double f1Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F1");
            Double f2Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F2");
            Double f3Attiva = getCategoriaConsumo(categorie, "Energia Attiva", "F3");
            Double f1Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F1");
            Double f2Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F2");
            Double f3Reattiva = getCategoriaConsumo(categorie, "Energia Reattiva", "F3");
            Double f1Potenza = getCategoriaConsumo(categorie, "Potenza", "F1");
            Double f2Potenza = getCategoriaConsumo(categorie, "Potenza", "F2");
            Double f3Potenza = getCategoriaConsumo(categorie, "Potenza", "F3");

            // Extract "Picco" and "Fuori Picco" data for the given month
            Map<String, Map<String, Double>> piccoData = piccoEFuoriPicco.getOrDefault(mese, new HashMap<>());
            Map<String, Double> picco = piccoData.getOrDefault("Picco", new HashMap<>());
            Map<String, Double> fuoriPicco = piccoData.getOrDefault("Fuori Picco", new HashMap<>());

            Double consumoPicco = picco.getOrDefault("kWh", 0.0);
            Double costoPicco = picco.getOrDefault("€", 0.0);
            Double consumoFuoriPicco = fuoriPicco.getOrDefault("kWh", 0.0);
            Double costoFuoriPicco = fuoriPicco.getOrDefault("€", 0.0);

            // Extract expenses
            Double spesaEnergia = spese.getOrDefault("Materia Energia", 0.0);
            Double spesaTrasporto = spese.getOrDefault("Trasporto e Gestione Contatore", 0.0);
            Double oneri = spese.getOrDefault("Oneri di Sistema", 0.0);
            Double imposte = spese.getOrDefault("Totale Imposte", 0.0);

            // Calculate totals
            Double totAttiva = (f1Attiva + f2Attiva + f3Attiva);
            Double totReattiva = (f1Reattiva + f2Reattiva + f3Reattiva);

            // Create and persist BollettaPod entity
            BollettaPod bolletta = new BollettaPod();
            bolletta.setIdPod(idPod);
            bolletta.setNomeBolletta(nomeBolletta);
            bolletta.setF1A(f1Attiva != null ? f1Attiva : 0.0);
            bolletta.setF2A(f2Attiva != null ? f2Attiva : 0.0);
            bolletta.setF3A(f3Attiva != null ? f3Attiva : 0.0);
            bolletta.setF1R(f1Reattiva != null ? f1Reattiva : 0.0);
            bolletta.setF2R(f2Reattiva != null ? f2Reattiva : 0.0);
            bolletta.setF3R(f3Reattiva != null ? f3Reattiva : 0.0);
            bolletta.setF1P(f1Potenza != null ? f1Potenza : 0.0);
            bolletta.setF2P(f2Potenza != null ? f2Potenza : 0.0);
            bolletta.setF3P(f3Potenza != null ? f3Potenza : 0.0);
            bolletta.setSpeseEnergia(spesaEnergia);
            bolletta.setTrasporti(spesaTrasporto);
            bolletta.setOneri(oneri);
            bolletta.setImposte(imposte);
            bolletta.setPeriodoInizio(new java.sql.Date(periodo.getInizio().getTime()));
            bolletta.setPeriodoFine(new java.sql.Date(periodo.getFine().getTime()));
            bolletta.setMese(mese);
            bolletta.setTotAttiva(totAttiva);
            bolletta.setTotReattiva(totReattiva);
            bolletta.setAnno(periodo.getAnno());
            bolletta.setPiccoKwh(consumoPicco);
            bolletta.setCostoPicco(costoPicco);
            bolletta.setFuoriPiccoKwh(consumoFuoriPicco);
            bolletta.setCostoFuoriPicco(costoFuoriPicco);

            bollettaRepo.persist(bolletta);
        }
    }


    private Double getCategoriaConsumo(Map<String, Map<String, Integer>> categorie, String categoria, String fascia) {
        return categorie.getOrDefault(categoria, Collections.emptyMap()).getOrDefault(fascia, 0).doubleValue();
    }

}
