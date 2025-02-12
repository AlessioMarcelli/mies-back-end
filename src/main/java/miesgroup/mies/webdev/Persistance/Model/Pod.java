package miesgroup.mies.webdev.Persistance.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "pod") // Nome corretto della tabella nel database
public class Pod extends PanacheEntityBase {

    @Id
    @Column(name = "Id_Pod", length = 14) // Nome corretto della colonna
    private String id;

    @Column(name = "Tensione_Alimentazione", nullable = false)
    private double tensioneAlimentazione;

    @Column(name = "Potenza_Impegnata", nullable = false)
    private double potenzaImpegnata;

    @Column(name = "Potenza_Disponibile", nullable = false)
    private double potenzaDisponibile;

    @ManyToOne
    @JoinColumn(name = "id_utente", nullable = false) // Foreign key verso `utente`
    private Cliente utente;

    @Column(name = "Sede")
    private String sede;

    @Column(name = "Nazione")
    private String nazione;

    @Column(name = "Tipo_Tensione", nullable = false)
    private String tipoTensione;

    // GETTER e SETTER

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getTensioneAlimentazione() {
        return tensioneAlimentazione;
    }

    public void setTensioneAlimentazione(double tensioneAlimentazione) {
        this.tensioneAlimentazione = tensioneAlimentazione;
    }

    public double getPotenzaImpegnata() {
        return potenzaImpegnata;
    }

    public void setPotenzaImpegnata(double potenzaImpegnata) {
        this.potenzaImpegnata = potenzaImpegnata;
    }

    public double getPotenzaDisponibile() {
        return potenzaDisponibile;
    }

    public void setPotenzaDisponibile(double potenzaDisponibile) {
        this.potenzaDisponibile = potenzaDisponibile;
    }

    public Cliente getUtente() {
        return utente;
    }

    public void setUtente(Cliente utente) {
        this.utente = utente;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public String getTipoTensione() {
        return tipoTensione;
    }

    public void setTipoTensione(String tipoTensione) {
        this.tipoTensione = tipoTensione;
    }
}
