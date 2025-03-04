package miesgroup.mies.webdev.Persistance.Model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.sql.Date;

@Entity
@Table(name = "bolletta_pod") // Nome corretto della tabella nel database
public class BollettaPod extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment su MySQL
    @Column(name = "Id_Bolletta") // Nome corretto della colonna ID
    private Integer id;

    @Column(name = "id_pod", nullable = false)
    private String idPod;

    @Column(name = "Nome_Bolletta", nullable = false)
    private String nomeBolletta;

    @Column(name = "F1_Attiva")
    private Double f1A;

    @Column(name = "F2_Attiva")
    private Double f2A;

    @Column(name = "F3_Attiva")
    private Double f3A;

    @Column(name = "F1_Reattiva")
    private Double f1R;

    @Column(name = "F2_Reattiva")
    private Double f2R;

    @Column(name = "F3_Reattiva")
    private Double f3R;

    @Column(name = "F1_Potenza")
    private Double f1P;

    @Column(name = "F2_Potenza")
    private Double f2P;

    @Column(name = "F3_Potenza")
    private Double f3P;

    @Column(name = "TOT_Attiva")
    private Double totAttiva;

    @Column(name = "TOT_Reattiva")
    private Double totReattiva;

    @Column(name = "Spese_Energia")
    private Double speseEnergia;

    @Column(name = "Spese_Trasporto")
    private Double trasporti;

    @Column(name = "Oneri")
    private Double oneri;

    @Column(name = "Imposte")
    private Double imposte;

    @Column(name = "Generation")
    private Double generation;

    @Column(name = "Dispacciamento")
    private Double dispacciamento;

    @Column(name = "Penali33")
    private Double penali33;

    @Column(name = "Penali75")
    private Double penali75;

    @Column(name = "Altro")
    private Double altro;

    @Column(name = "Periodo_Inizio", nullable = false)
    private Date periodoInizio;

    @Column(name = "Periodo_Fine", nullable = false)
    private Date periodoFine;

    @Column(name = "Anno", nullable = false)
    private String anno;

    @Column(name = "Mese")
    private String mese;

    @Column(name = "Verifica_Oneri")
    private Double verificaOneri;

    @Column(name = "Verifica_Trasporti")
    private Double verificaTrasporti;

    @Column(name = "Verifica_Imposte")
    private Double verificaImposte;

    @Column(name = "picco_kwh")
    private Double piccoKwh;

    @Column(name = "fuori_picco_kwh")
    private Double fuoriPiccoKwh;

    @Column(name = "€_picco")
    private Double costoPicco;

    @Column(name = "€_fuori_picco")
    private Double costoFuoriPicco;

    @Column(name = "verifica_picco")
    private Double verificaPicco;

    @Column(name = "verifica_fuori_picco")
    private Double verificaFuoriPicco;

    @Column(name = "tot_attiva_perdite")
    private Double totAttivaPerdite;

    // GETTER e SETTER

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdPod() {
        return idPod;
    }

    public void setIdPod(String idPod) {
        this.idPod = idPod;
    }

    public String getNomeBolletta() {
        return nomeBolletta;
    }

    public void setNomeBolletta(String nomeBolletta) {
        this.nomeBolletta = nomeBolletta;
    }

    public Double getF1A() {
        return f1A;
    }

    public void setF1A(Double f1A) {
        this.f1A = f1A;
    }

    public Double getF2A() {
        return f2A;
    }

    public void setF2A(Double f2A) {
        this.f2A = f2A;
    }

    public Double getF3A() {
        return f3A;
    }

    public void setF3A(Double f3A) {
        this.f3A = f3A;
    }

    public Double getF1R() {
        return f1R;
    }

    public void setF1R(Double f1R) {
        this.f1R = f1R;
    }

    public Double getF2R() {
        return f2R;
    }

    public void setF2R(Double f2R) {
        this.f2R = f2R;
    }

    public Double getF3R() {
        return f3R;
    }

    public void setF3R(Double f3R) {
        this.f3R = f3R;
    }

    public Double getF1P() {
        return f1P;
    }

    public void setF1P(Double f1P) {
        this.f1P = f1P;
    }

    public Double getF2P() {
        return f2P;
    }

    public void setF2P(Double f2P) {
        this.f2P = f2P;
    }

    public Double getF3P() {
        return f3P;
    }

    public void setF3P(Double f3P) {
        this.f3P = f3P;
    }

    public Double getTotAttiva() {
        return totAttiva;
    }

    public void setTotAttiva(Double totAttiva) {
        this.totAttiva = totAttiva;
    }

    public Double getTotReattiva() {
        return totReattiva;
    }

    public void setTotReattiva(Double totReattiva) {
        this.totReattiva = totReattiva;
    }

    public Double getSpeseEnergia() {
        return speseEnergia;
    }

    public void setSpeseEnergia(Double speseEnergia) {
        this.speseEnergia = speseEnergia;
    }

    public Double getTrasporti() {
        return trasporti;
    }

    public void setTrasporti(Double trasporti) {
        this.trasporti = trasporti;
    }

    public Double getOneri() {
        return oneri;
    }

    public void setOneri(Double oneri) {
        this.oneri = oneri;
    }

    public Double getImposte() {
        return imposte;
    }

    public void setImposte(Double imposte) {
        this.imposte = imposte;
    }

    public Double getGeneration() {
        return generation;
    }

    public void setGeneration(Double generation) {
        this.generation = generation;
    }

    public Double getDispacciamento() {
        return dispacciamento;
    }

    public void setDispacciamento(Double dispacciamento) {
        this.dispacciamento = dispacciamento;
    }

    public Double getPenali33() {
        return penali33;
    }

    public void setPenali33(Double penali33) {
        this.penali33 = penali33;
    }

    public Double getPenali75() {
        return penali75;
    }

    public void setPenali75(Double penali75) {
        this.penali75 = penali75;
    }

    public Double getAltro() {
        return altro;
    }

    public void setAltro(Double altro) {
        this.altro = altro;
    }

    public Date getPeriodoInizio() {
        return periodoInizio;
    }

    public void setPeriodoInizio(Date periodoInizio) {
        this.periodoInizio = periodoInizio;
    }

    public Date getPeriodoFine() {
        return periodoFine;
    }

    public void setPeriodoFine(Date periodoFine) {
        this.periodoFine = periodoFine;
    }

    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public String getMese() {
        return mese;
    }

    public void setMese(String mese) {
        this.mese = mese;
    }

    public Double getVerificaOneri() {
        return verificaOneri;
    }

    public void setVerificaOneri(Double verificaOnneri) {
        this.verificaOneri = verificaOnneri;
    }

    public Double getVerificaTrasporti() {
        return verificaTrasporti;
    }

    public void setVerificaTrasporti(Double verificaTrasporti) {
        this.verificaTrasporti = verificaTrasporti;
    }

    public Double getVerificaImposte() {
        return verificaImposte;
    }

    public void setVerificaImposte(Double verificaImposte) {
        this.verificaImposte = verificaImposte;
    }

    public Double getPiccoKwh() {
        return piccoKwh;
    }

    public void setPiccoKwh(Double picco) {
        this.piccoKwh = picco;
    }

    public Double getFuoriPiccoKwh() {
        return fuoriPiccoKwh;
    }

    public void setFuoriPiccoKwh(Double fuoriPicco) {
        this.fuoriPiccoKwh = fuoriPicco;
    }

    public Double getCostoPicco() {
        return costoPicco;
    }

    public void setCostoPicco(Double costoPicco) {
        this.costoPicco = costoPicco;
    }

    public Double getCostoFuoriPicco() {
        return costoFuoriPicco;
    }

    public void setCostoFuoriPicco(Double costoFuoriPicco) {
        this.costoFuoriPicco = costoFuoriPicco;
    }

    public Double getVerificaPicco() {
        return verificaPicco;
    }

    public void setVerificaPicco(Double verificaPicco) {
        this.verificaPicco = verificaPicco;
    }

    public Double getVerificaFuoriPicco() {
        return verificaFuoriPicco;
    }

    public void setVerificaFuoriPicco(Double verificaFuoriPicco) {
        this.verificaFuoriPicco = verificaFuoriPicco;
    }

    public Double getTotAttivaPerdite() {
        return totAttivaPerdite;
    }

    public void setTotAttivaPerdite(Double totAttivaPerdite) {
        this.totAttivaPerdite = totAttivaPerdite;
    }
}
