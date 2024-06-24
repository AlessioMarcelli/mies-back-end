package miesgroup.mies.webdev.Persistance;

public class Pod {
    private String id;
    private double Tensione_Alimentazione;
    private double potenza_Impegnata;
    private double Potenza_Disponibile;
    private int id_utente;//fk

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getTensione_Alimentazione() {
        return Tensione_Alimentazione;
    }

    public void setTensione_Alimentazione(double tensione_Alimentazione) {
        Tensione_Alimentazione = tensione_Alimentazione;
    }

    public double getPotenza_Impegnata() {
        return potenza_Impegnata;
    }

    public void setPotenza_Impegnata(double potenza_Impegnata) {
        this.potenza_Impegnata = potenza_Impegnata;
    }

    public double getPotenza_Disponibile() {
        return Potenza_Disponibile;
    }

    public void setPotenza_Disponibile(double potenza_Disponibile) {
        Potenza_Disponibile = potenza_Disponibile;
    }

    public int getId_utente() {
        return id_utente;
    }

    public void setId_utente(int id_utente) {
        this.id_utente = id_utente;
    }
}
