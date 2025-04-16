package miesgroup.mies.webdev.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "GeneralAlert")
public class GeneralAlert extends PanacheEntityBase {

    @Id
    @Column(name = "Id_Utente", insertable = false, updatable = false)
    private Integer idUtente;

    private Double maxPriceValue;
    private Double minPriceValue;

    private String frequencyA;
    private Boolean checkModality;

    @ManyToOne
    @JoinColumn(name = "Id_Utente")
    private Cliente utente;

    public GeneralAlert() {}

    public GeneralAlert(Double maxPriceValue, Double minPriceValue, Integer idUtente, String frequencyA, Boolean checkModality) {
        this.maxPriceValue = maxPriceValue;
        this.minPriceValue = minPriceValue;
        this.idUtente = idUtente;
        this.frequencyA = frequencyA;
        this.checkModality = checkModality;
    }

    // Getter e Setter

    public Double getMaxPriceValue(){
        return maxPriceValue;
    }

    public void setMaxPriceValue(Double maxPriceValue){
        this.maxPriceValue = maxPriceValue;
    }

    public Double getMinPriceValue(){
        return minPriceValue;
    }

    public void setMinPriceValue(Double minPriceValue){
        this.minPriceValue = minPriceValue;
    }

    public String getFrequencyA(){
        return frequencyA;
    }

    public void setFrequencyA(String frequencyA){
        this.frequencyA = frequencyA;
    }

    public Boolean getCheckModality(){
        return checkModality;
    }

    public void setCheckModality(Boolean checkModality){
        this.checkModality = checkModality;
    }

    public Cliente getUtente(){
        return utente;
    }

    public void setUtente(Cliente utente){
        this.utente = utente;
    }
}
