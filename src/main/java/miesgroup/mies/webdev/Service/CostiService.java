package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Persistance.Repository.CostiRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CostiService {
    private final CostiRepo costiRepo;

    public CostiService(CostiRepo costiRepo) {
        this.costiRepo = costiRepo;
    }

    @Transactional
    public boolean createCosto(String descrizione, String categoria, String unitaMisura, Integer trimestre, String anno, float valore, String tipoTensione, String classeAgevolazione) throws SQLException {
        Costi costo = new Costi();
        costo.setDescrizione(descrizione);
        costo.setCategoria(categoria);
        costo.setCosto(valore);
        costo.setUnitaMisura(unitaMisura);
        costo.setTrimestre(trimestre);
        costo.setAnno(anno);
        costo.setIntervalloPotenza(tipoTensione);
        costo.setClasseAgevolazione(classeAgevolazione);
        return costiRepo.aggiungiCosto(costo);
    }


    @Transactional
    public List<Costi> getAllCosti() {
        return costiRepo.getAllCosti();
    }


    @Transactional
    public Costi getSum(String intervalloPotenza) {
        return costiRepo.getSum(intervalloPotenza);
    }

    @Transactional
    public void deleteCosto(int id) {
        costiRepo.deleteCosto(id);
    }

    @Transactional
    public boolean updateCosto(int id, String descrizione, String categoria, String unitaMisura, int trimestre, String anno, float costo, String intervalloPotenza, String classeAgevolazione) {
        Costi c = new Costi();
        c.setId(id);
        c.setDescrizione(descrizione);
        c.setCategoria(categoria);
        c.setUnitaMisura(unitaMisura);
        c.setTrimestre(trimestre);
        c.setAnno(anno);
        c.setCosto(costo);
        c.setIntervalloPotenza(intervalloPotenza);
        c.setClasseAgevolazione(classeAgevolazione);
        return costiRepo.updateCosto(c);
    }

    // Metodo che genera il file Excel
    @Transactional
    public ByteArrayOutputStream generateExcelFile() throws Exception {
        List<Costi> costiList = getAllCosti();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Costi");

            // Aggiungi l'intestazione
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Descrizione", "Unità di Misura", "Trimestre", "Anno",
                    "Costo", "Categoria", "Intervallo Potenza",
                    "Classe Agevolazione"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderCellStyle(workbook));
            }

            // Aggiungi i dati
            int rowIndex = 1;
            for (Costi costo : costiList) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(1).setCellValue(costo.getDescrizione());
                row.createCell(2).setCellValue(costo.getUnitaMisura());
                row.createCell(3).setCellValue(costo.getTrimestre());
                row.createCell(4).setCellValue(costo.getAnno());
                row.createCell(5).setCellValue(costo.getCosto());
                row.createCell(6).setCellValue(costo.getCategoria());
                row.createCell(7).setCellValue(costo.getIntervalloPotenza());
                row.createCell(8).setCellValue(costo.getClasseAgevolazione());
            }

            // Scrivi il workbook nel flusso di output
            workbook.write(out);

            return out;
        }
    }

    @Transactional
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    @Transactional
    public void processExcelFile(InputStream excelInputStream) {
        try (Workbook workbook = WorkbookFactory.create(excelInputStream)) {
            Sheet sheet = workbook.getSheet("Costi");
            if (sheet == null) {
                throw new IllegalArgumentException("Il foglio 'Costi' non esiste nel file Excel.");
            }

            // Recupera tutti i costi già presenti nel database
            List<Costi> existingCosti = getAllCosti();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // Saltiamo l'intestazione
                    continue;
                }

                String descrizione = getCellValue(row.getCell(0));
                String unitaMisura = getCellValue(row.getCell(1));
                int trimestre = Integer.parseInt(getCellValue(row.getCell(2)));
                String anno = getCellValue(row.getCell(3));
                float costo = Float.parseFloat(getCellValue(row.getCell(4)));
                String categoria = getCellValue(row.getCell(5));
                String intervalloPotenza = getCellValue(row.getCell(6));
                String classeAgevolazione = getCellValue(row.getCell(7));

                // Controlla se il costo esiste già
                if (!existsInList(existingCosti, descrizione, unitaMisura, trimestre, anno, costo, categoria, intervalloPotenza, classeAgevolazione)) {
                    // Inserisce una nuova riga nel database
                    Costi dettaglioCosto = new Costi();
                    dettaglioCosto.setDescrizione(descrizione);
                    dettaglioCosto.setUnitaMisura(unitaMisura);
                    dettaglioCosto.setTrimestre(trimestre);
                    dettaglioCosto.setAnno(anno);
                    dettaglioCosto.setCosto(costo);
                    dettaglioCosto.setCategoria(categoria);
                    dettaglioCosto.setIntervalloPotenza(intervalloPotenza);
                    dettaglioCosto.setClasseAgevolazione(classeAgevolazione);

                    costiRepo.save(dettaglioCosto);

                    // Aggiunge il nuovo costo alla lista per evitare duplicati
                    existingCosti.add(dettaglioCosto);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'elaborazione del file Excel", e);
        }
    }

    @Transactional
    private boolean existsInList(List<Costi> existingCosti, String descrizione, String unitaMisura, int trimestre, String anno, float costo, String categoria, String intervalloPotenza, String classeAgevolazione) {
        for (Costi costoEsistente : existingCosti) {
            if (areEqual(costoEsistente.getDescrizione(), descrizione) &&
                    areEqual(costoEsistente.getUnitaMisura(), unitaMisura) &&
                    costoEsistente.getTrimestre() == trimestre &&
                    areEqual(costoEsistente.getAnno(), anno) &&
                    Float.compare(costoEsistente.getCosto(), costo) == 0 && // Confronto sicuro per float
                    areEqual(costoEsistente.getCategoria(), categoria) &&
                    areEqual(costoEsistente.getIntervalloPotenza(), intervalloPotenza) &&
                    areEqual(costoEsistente.getClasseAgevolazione(), classeAgevolazione)) {
                return true; // Duplicato trovato
            }
        }
        return false; // Non trovato
    }

    private boolean areEqual(String value1, String value2) {
        if (value1 == null && value2 == null) {
            return true; // Entrambi null
        }
        if (value1 == null || value2 == null) {
            return false; // Uno è null, l'altro no
        }
        return value1.equals(value2); // Confronto normale
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if ((cell.getNumericCellValue() % 1) == 0) {
                    return String.valueOf((int) cell.getNumericCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}