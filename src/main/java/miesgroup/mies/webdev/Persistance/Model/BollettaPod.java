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
    private double f1A;

    @Column(name = "F2_Attiva")
    private double f2A;

    @Column(name = "F3_Attiva")
    private double f3A;

    @Column(name = "F1_Reattiva")
    private double f1R;

    @Column(name = "F2_Reattiva")
    private double f2R;

    @Column(name = "F3_Reattiva")
    private double f3R;

    @Column(name = "F1_Potenza")
    private double f1P;

    @Column(name = "F2_Potenza")
    private double f2P;

    @Column(name = "F3_Potenza")
    private double f3P;

    @Column(name = "TOT_Attiva")
    private double totAttiva;

    @Column(name = "TOT_Reattiva")
    private double totReattiva;

    @Column(name = "Spese_Energia")
    private double speseEnergia;

    @Column(name = "Spese_Trasporto")
    private double trasporti;

    @Column(name = "Oneri")
    private double oneri;

    @Column(name = "Imposte")
    private double imposte;

    @Column(name = "Generation")
    private double generation;

    @Column(name = "Dispacciamento")
    private double grandezzeTrasporti;

    @Column(name = "Penali33")
    private double penali33;

    @Column(name = "Penali75")
    private double penali75;

    @Column(name = "Altro")
    private double altro;

    @Column(name = "Periodo_Inizio", nullable = false)
    private Date periodoInizio;

    @Column(name = "Periodo_Fine", nullable = false)
    private Date periodoFine;

    @Column(name = "Anno", nullable = false)
    private String anno;

    @Column(name = "Mese")
    private String mese;

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

    public double getF1A() {
        return f1A;
    }

    public void setF1A(double f1A) {
        this.f1A = f1A;
    }

    public double getF2A() {
        return f2A;
    }

    public void setF2A(double f2A) {
        this.f2A = f2A;
    }

    public double getF3A() {
        return f3A;
    }

    public void setF3A(double f3A) {
        this.f3A = f3A;
    }

    public double getF1R() {
        return f1R;
    }

    public void setF1R(double f1R) {
        this.f1R = f1R;
    }

    public double getF2R() {
        return f2R;
    }

    public void setF2R(double f2R) {
        this.f2R = f2R;
    }

    public double getF3R() {
        return f3R;
    }

    public void setF3R(double f3R) {
        this.f3R = f3R;
    }

    public double getF1P() {
        return f1P;
    }

    public void setF1P(double f1P) {
        this.f1P = f1P;
    }

    public double getF2P() {
        return f2P;
    }

    public void setF2P(double f2P) {
        this.f2P = f2P;
    }

    public double getF3P() {
        return f3P;
    }

    public void setF3P(double f3P) {
        this.f3P = f3P;
    }

    public double getTotAttiva() {
        return totAttiva;
    }

    public void setTotAttiva(double totAttiva) {
        this.totAttiva = totAttiva;
    }

    public double getTotReattiva() {
        return totReattiva;
    }

    public void setTotReattiva(double totReattiva) {
        this.totReattiva = totReattiva;
    }

    public double getSpeseEnergia() {
        return speseEnergia;
    }

    public void setSpeseEnergia(double speseEnergia) {
        this.speseEnergia = speseEnergia;
    }

    public double getTrasporti() {
        return trasporti;
    }

    public void setTrasporti(double trasporti) {
        this.trasporti = trasporti;
    }

    public double getOneri() {
        return oneri;
    }

    public void setOneri(double oneri) {
        this.oneri = oneri;
    }

    public double getImposte() {
        return imposte;
    }

    public void setImposte(double imposte) {
        this.imposte = imposte;
    }

    public double getGeneration() {
        return generation;
    }

    public void setGeneration(double generation) {
        this.generation = generation;
    }

    public double getGrandezzeTrasporti() {
        return grandezzeTrasporti;
    }

    public void setGrandezzeTrasporti(double grandezzeTrasporti) {
        this.grandezzeTrasporti = grandezzeTrasporti;
    }

    public double getPenali33() {
        return penali33;
    }

    public void setPenali33(double penali33) {
        this.penali33 = penali33;
    }

    public double getPenali75() {
        return penali75;
    }

    public void setPenali75(double penali75) {
        this.penali75 = penali75;
    }

    public double getAltro() {
        return altro;
    }

    public void setAltro(double altro) {
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
}
