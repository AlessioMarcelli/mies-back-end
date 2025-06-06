package miesgroup.mies.webdev.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "dettaglio_costo") // Nome corretto della tabella
public class Costi extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment su MySQL
    @Column(name = "Id_Costo") // Nome corretto della colonna ID
    private Integer id;

    @Column(name = "Descrizione", nullable = false)
    private String descrizione;

    @Column(name = "Unità_Misura", nullable = false)
    private String unitaMisura;

    @Column(name = "Trimestrale")
    private Integer trimestre;

    @Column(name = "Annuale")
    private String anno;

    @Column(name = "Costo")
    private Double costo;

    @Column(name = "Categoria")
    private String categoria;

    @Column(name = "Intervallo_Potenza")
    private String intervalloPotenza;

    @Column(name = "Classe_Agevolazione")
    private String classeAgevolazione;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "Data_Inserimento", columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private Date dataInserimento;

    @Column(name = "anno_riferimento")
    private String annoRiferimento;

    // Costruttore di default
    public Costi() {
    }

    // GETTER e SETTER
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public Integer getTrimestre() {
        return trimestre;
    }

    public void setTrimestre(Integer trimestre) {
        this.trimestre = trimestre;
    }

    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getIntervalloPotenza() {
        return intervalloPotenza;
    }

    public void setIntervalloPotenza(String intervalloPotenza) {
        this.intervalloPotenza = intervalloPotenza;
    }

    public String getClasseAgevolazione() {
        return classeAgevolazione;
    }

    public void setClasseAgevolazione(String classeAgevolazione) {
        this.classeAgevolazione = classeAgevolazione;
    }

    public Date getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(Date dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public String getAnnoRiferimento() {
        return annoRiferimento;
    }

    public void setAnnoRiferimento(String annoRiferimento) {
        this.annoRiferimento = annoRiferimento;
    }
}
