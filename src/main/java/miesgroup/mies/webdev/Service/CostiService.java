package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Model.Costi;
import miesgroup.mies.webdev.Repository.CostiRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
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
    public List<Costi> getAllCosti(Integer idSessione) {
        if (idSessione == null) {
            throw new IllegalArgumentException("ID sessione mancante");
        }
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
        List<Costi> costiList = costiRepo.getAllCosti();

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Costi");

            // Aggiungi l'intestazione
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Descrizione", "Unità di Misura", "Trimestre", "Anno",
                    "Costo", "Categoria", "Intervallo Potenza",
                    "Classe Agevolazione", "Anno di riferimento"
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
                row.createCell(0).setCellValue(costo.getDescrizione());
                row.createCell(1).setCellValue(costo.getUnitaMisura());
                row.createCell(2).setCellValue(costo.getTrimestre());
                row.createCell(3).setCellValue(costo.getAnno());
                row.createCell(4).setCellValue(costo.getCosto());
                row.createCell(5).setCellValue(costo.getCategoria());
                row.createCell(6).setCellValue(costo.getIntervalloPotenza());
                row.createCell(7).setCellValue(costo.getClasseAgevolazione());
                row.createCell(8).setCellValue(costo.getAnnoRiferimento());
            }

            // Scrivi il workbook nel flusso di output
            workbook.write(out);

            return out;
        }
    }


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

            List<Costi> existingCosti = costiRepo.getAllCosti();
            int emptyRowCount = 0; // Contatore di righe vuote consecutive

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue; // Salta l'intestazione
                }

                // Controlla se la riga è completamente vuota
                boolean isEmptyRow = true;
                for (int i = 0; i <= 7; i++) {
                    if (!getCellValue(row.getCell(i)).isEmpty()) {
                        isEmptyRow = false;
                        break;
                    }
                }

                if (isEmptyRow) {
                    emptyRowCount++;
                    if (emptyRowCount >= 3) { // Se trova 3 righe vuote consecutive, interrompe il ciclo
                        System.out.println("Rilevate 3 righe vuote consecutive. Interruzione del parsing.");
                        break;
                    }
                    continue; // Salta la riga vuota
                } else {
                    emptyRowCount = 0; // Reset del contatore se la riga non è vuota
                }

                // Estrai i valori con gestione degli errori
                String descrizione = getCellValue(row.getCell(0));
                String unitaMisura = getCellValue(row.getCell(1));
                String trimestreStr = getCellValue(row.getCell(2));
                String annoStr = getCellValue(row.getCell(3));
                String costoStr = getCellValue(row.getCell(4));
                String categoria = getCellValue(row.getCell(5));
                String intervalloPotenza = getCellValue(row.getCell(6));
                String classeAgevolazione = getCellValue(row.getCell(7));
                String annoRiferimento = getCellValue(row.getCell(8));

                int trimestre = trimestreStr.isEmpty() ? 0 : (int) Double.parseDouble(trimestreStr);
                float costo = costoStr.isEmpty() ? 0.0f : Float.parseFloat(costoStr);

                if (!existsInList(existingCosti, descrizione, unitaMisura, trimestre, annoStr, costo, categoria, intervalloPotenza, classeAgevolazione, annoRiferimento)) {
                    Costi dettaglioCosto = new Costi();
                    dettaglioCosto.setDescrizione(descrizione);
                    dettaglioCosto.setUnitaMisura(unitaMisura);
                    dettaglioCosto.setTrimestre(trimestre);
                    dettaglioCosto.setAnno(annoStr.replaceAll("\\.0+$", ""));
                    dettaglioCosto.setCosto(costo);
                    dettaglioCosto.setCategoria(categoria);
                    dettaglioCosto.setIntervalloPotenza(intervalloPotenza);
                    dettaglioCosto.setClasseAgevolazione(classeAgevolazione);
                    dettaglioCosto.setAnnoRiferimento(annoRiferimento.replaceAll("\\.0+$", ""));

                    costiRepo.persist(dettaglioCosto);
                    existingCosti.add(dettaglioCosto);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'elaborazione del file Excel", e);
        }
    }


    private boolean existsInList(List<Costi> existingCosti, String descrizione, String unitaMisura, int trimestre, String anno, float costo, String categoria, String intervalloPotenza, String classeAgevolazione, String annoRiferimento) {
        for (Costi costoEsistente : existingCosti) {
            if (
                    areEqual(costoEsistente.getDescrizione(), descrizione) &&
                            areEqual(costoEsistente.getUnitaMisura(), unitaMisura) &&
                            costoEsistente.getTrimestre() == trimestre &&
                            areEqual(costoEsistente.getAnno(), anno) &&
                            Float.compare(costoEsistente.getCosto(), costo) == 0 && // Confronto sicuro per float
                            areEqual(costoEsistente.getCategoria(), categoria) &&
                            areEqual(costoEsistente.getIntervalloPotenza(), intervalloPotenza) &&
                            areEqual(costoEsistente.getClasseAgevolazione(), classeAgevolazione) &&
                            areEqual(costoEsistente.getAnnoRiferimento(), annoRiferimento)
            ) {
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
        if (cell == null) {
            return ""; // Restituisce stringa vuota se la cella è null
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return ""; // Restituisce stringa vuota se la cella è vuota
            default:
                return "";
        }
    }

}