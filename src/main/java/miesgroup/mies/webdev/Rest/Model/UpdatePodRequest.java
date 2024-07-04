package miesgroup.mies.webdev.Rest.Model;

public class UpdatePodRequest {
    private String idPod;
    private String sede;
    private String nazione;

    public String getIdPod() {
        return idPod;
    }

    public void setIdPod(String idPod) {
        this.idPod = idPod;
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
}
