package miesgroup.mies.webdev.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "costo_articolo")
public class CostoArticolo extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome_articolo")
    private String nomeArticolo;

    @Column(name = "costo_unitario")
    private Double costoUnitario;

    @ManyToOne
    @JoinColumn(name = "nome_bolletta", referencedColumnName = "nome_bolletta")
    private BollettaPod nomeBolletta;

    @Column(name = "mese")
    private String mese;

    // GETTER e SETTER

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeArticolo() {
        return nomeArticolo;
    }

    public void setNomeArticolo(String nomeArticolo) {
        this.nomeArticolo = nomeArticolo;
    }

    public Double getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(Double costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public BollettaPod getNomeBolletta() {
        return nomeBolletta;
    }

    public void setNomeBolletta(BollettaPod idBolletta) {
        this.nomeBolletta = idBolletta;
    }

    public String getMese() {
        return mese;
    }

    public void setMese(String mese) {
        this.mese = mese;
    }
}
