package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Persistance.Repository.CostiRepo;
import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;

@ApplicationScoped
public class CostiService {
    private final CostiRepo costiRepo;

    public CostiService(CostiRepo costiRepo) {
        this.costiRepo = costiRepo;
    }

    public void createCosto(String descrizione, String categoria, String unitaMisura, Integer trimestre, String anno, float valore, String tipoTensione, String classeAgevolazione) throws SQLException {
        Costi costo = new Costi();
        costo.setDescrizione(descrizione);
        costo.setCategoria(categoria);
        costo.setCosto(valore);
        costo.setUnitaMisura(unitaMisura);
        costo.setTrimestre(trimestre);
        costo.setAnno(anno);
        costo.setIntervalloPotenza(tipoTensione);
        costo.setClasseAgevolazione(classeAgevolazione);
        costiRepo.aggiungiCosto(costo);
    }


    public ArrayList<Costi> getAllCosti() {
        return costiRepo.getAllCosti();
    }


    public Costi getSum(String intervalloPotenza) {
        return costiRepo.getSum(intervalloPotenza);
    }


    public void deleteCosto(int id) {
        costiRepo.deleteCosto(id);
    }

    public void readExcelFile(InputStream inputStream) throws Exception {
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // Prendi il primo foglio
        ArrayList<String> costiExcle = new ArrayList<>();
        int i = 0;
        for (Row row : sheet) {
            for (Cell cell : row) {
                switch (cell.getCellType()) {
                    case STRING:
                        if (i == 1) {
                            costiExcle.add(cell.getStringCellValue());
                        }
                        break;
                    case NUMERIC:
                        if (i == 1) {
                            String value = String.valueOf(cell.getNumericCellValue());
                            costiExcle.add(value);
                            System.out.println(cell.getNumericCellValue());
                        }
                        break;
                }
            }
            i = 1;
        }
        workbook.close();
        costiRepo.aggiungiCostoFromExcel(costiExcle);
    }
}