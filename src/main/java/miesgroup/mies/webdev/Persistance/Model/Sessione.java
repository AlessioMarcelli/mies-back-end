package miesgroup.mies.webdev.Persistance.Model;

import java.sql.Timestamp;

public class Sessione {
    private int id;
    private int utenteId;
    private Timestamp Data_Sessione;

    public Sessione() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtenteId() {
        return utenteId;
    }

    public void setUtenteId(int utenteId) {
        this.utenteId = utenteId;
    }

    public Timestamp getData_Sessione() {
        return Data_Sessione;
    }

    public void setData_Sessione(Timestamp dataCreazione) {
        this.Data_Sessione = dataCreazione;
    }

}