package miesgroup.mies.webdev.Rest.Model;

public class ClienteResponse {

    private String username;
    private String email;
    private String pIva;
    private String sedeLegale;
    private String telefono;
    private String stato;
    private String tipologia;

    public ClienteResponse(String username, String email, String pIva, String sedeLegale, String telefono, String stato, String tipologia) {
        this.username = username;
        this.email = email;
        this.pIva = pIva;
        this.sedeLegale = sedeLegale;
        this.telefono = telefono;
        this.stato = stato;
        this.tipologia = tipologia;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getpIva() {
        return pIva;
    }

    public String getSedeLegale() {
        return sedeLegale;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getStato() {
        return stato;
    }

    public String getTipologia() {
        return tipologia;
    }
}
