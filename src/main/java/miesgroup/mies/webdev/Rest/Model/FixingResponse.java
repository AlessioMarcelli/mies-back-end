package miesgroup.mies.webdev.Rest.Model;

import java.time.LocalDate;

public class FixingResponse {
    private Integer id;

    private String descrizione;

    private Double costo;

    private String unitaMisura;

    private LocalDate periodoInizio;

    private LocalDate periodoFine;


    public FixingResponse() {
    }

    public FixingResponse(Integer id, String descrizione, Double costo, String unitaMisura, LocalDate periodoInizio, LocalDate periodoFine) {
        this.id = id;
        this.descrizione = descrizione;
        this.costo = costo;
        this.unitaMisura = unitaMisura;
        this.periodoInizio = periodoInizio;
        this.periodoFine = periodoFine;
    }

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

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public LocalDate getPeriodoInizio() {
        return periodoInizio;
    }

    public void setPeriodoInizio(LocalDate periodoInizio) {
        this.periodoInizio = periodoInizio;
    }

    public LocalDate getPeriodoFine() {
        return periodoFine;
    }

    public void setPeriodoFine(LocalDate periodoFine) {
        this.periodoFine = periodoFine;
    }
}
