package miesgroup.mies.webdev.Rest.Model;

import miesgroup.mies.webdev.Model.Costi;

import java.time.LocalDate;

public class CostiDTO {
    public String descrizione;
    public String unitaMisura;
    public Integer trimestre;
    public String anno;
    public Double costo;
    public String categoria;
    public String intervalloPotenza;
    public String classeAgevolazione;
    public String annoRiferimento;

    public CostiDTO(Costi entity) {
        this.descrizione = entity.getDescrizione();
        this.unitaMisura = entity.getUnitaMisura();
        this.trimestre = entity.getTrimestre();
        this.anno = entity.getAnno();
        this.costo = entity.getCosto();
        this.categoria = entity.getCategoria();
        this.intervalloPotenza = entity.getIntervalloPotenza();
        this.classeAgevolazione = entity.getClasseAgevolazione();
        this.annoRiferimento = entity.getAnnoRiferimento();
    }
}
