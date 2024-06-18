package miesgroup.mies.webdev.Service.Exception;

public class WrongUsernameOrPasswordException extends Exception {
    public WrongUsernameOrPasswordException() {
        super("Username o password errati");
    }
}