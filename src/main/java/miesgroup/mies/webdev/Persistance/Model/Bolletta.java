package miesgroup.mies.webdev.Persistance.Model;

public class Bolletta {
    private int id;
    private String id_pod;
    private double f1A;
    private double f2A;
    private double f3A;
    private double f1R;
    private double f2R;
    private double f3R;
    private double TOT_Attiva;
    private double TOT_Reattiva;
    private double Spese_Energia;
    private double trasporti;
    private double oneri;
    private double imposte;
    private double Generation;
    private double grandezzeTrasporti;
    private double penali;
    private double altro;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getId_pod() {
        return id_pod;
    }

    public void setId_pod(String id_pod) {
        this.id_pod = id_pod;
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

    public double getTOT_Attiva() {
        return TOT_Attiva;
    }

    public void setTOT_Attiva(double TOT_Attiva) {
        this.TOT_Attiva = TOT_Attiva;
    }

    public double getTOT_Reattiva() {
        return TOT_Reattiva;
    }

    public void setTOT_Reattiva(double TOT_Reattiva) {
        this.TOT_Reattiva = TOT_Reattiva;
    }

    public double getSpese_Energia() {
        return Spese_Energia;
    }

    public void setSpese_Energia(double spese_Energia) {
        Spese_Energia = spese_Energia;
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
        return Generation;
    }

    public void setGeneration(double generation) {
        Generation = generation;
    }

    public double getGrandezzeTrasporti() {
        return grandezzeTrasporti;
    }

    public void setGrandezzeTrasporti(double grandezzeTrasporti) {
        this.grandezzeTrasporti = grandezzeTrasporti;
    }

    public double getPenali() {
        return penali;
    }

    public void setPenali(double penali) {
        this.penali = penali;
    }

    public double getAltro() {
        return altro;
    }

    public void setAltro(double altro) {
        this.altro = altro;
    }
}