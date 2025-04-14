package miesgroup.mies.webdev.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "MonthlyAlert")
public class MonthlyAlert extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double maxPriceValue;

    private Double minPriceValue;

    private Integer Id_Utente;

    private String frequencyA;

    private Boolean checkModality;

    @ManyToOne
    @JoinColumn(name = "Id_Utente")
    private Cliente utente;

    // Costruttori
    public MonthlyAlert() {}

    public MonthlyAlert(Double maxPriceValue, Double minPriceValue, Integer Id_Utente, String frequencyA, Boolean checkModality) {
        this.maxPriceValue = maxPriceValue;
        this.minPriceValue = minPriceValue;
        this.Id_Utente = Id_Utente;
        this.frequencyA = frequencyA;
        this.checkModality = checkModality;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public Double getMaxPriceValue() {
        return maxPriceValue;
    }

    public void setMaxPriceValue(Double maxPriceValue) {
        this.maxPriceValue = maxPriceValue;
    }

    public Double getMinPriceValue() {
        return minPriceValue;
    }

    public void setMinPriceValue(Double minPriceValue) {
        this.minPriceValue = minPriceValue;
    }

    public Integer getId_Utente() {
        return Id_Utente;
    }

    public void setId_Utente(Integer id_Utente) {
        Id_Utente = id_Utente;
    }

    public String getFrequencyA() {
        return frequencyA;
    }

    public void setFrequencyA(String frequencyA) {
        this.frequencyA = frequencyA;
    }

    public Boolean getCheckModality() {
        return checkModality;
    }

    public void setCheckModality(Boolean checkModality) {
        this.checkModality = checkModality;
    }

    public Cliente getUtente() {
        return utente;
    }

    public void setUtente(Cliente utente) {
        this.utente = utente;
    }

}

