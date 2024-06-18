package miesgroup.mies.webdev.Service.Exception;

public class ClienteCreationException extends Exception {
    public ClienteCreationException() {
        super("Utente con questo username esiste già");
    }
}
