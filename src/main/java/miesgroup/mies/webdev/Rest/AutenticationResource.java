package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Persistance.Model.Sessione;
import miesgroup.mies.webdev.Persistance.Repository.ClienteRepo;
import miesgroup.mies.webdev.Persistance.Repository.SessionRepo;
import miesgroup.mies.webdev.Rest.Model.LoginRequest;
import miesgroup.mies.webdev.Service.AutenticationService;
import miesgroup.mies.webdev.Service.Exception.ClienteCreationException;
import miesgroup.mies.webdev.Service.Exception.SessionCreationException;
import miesgroup.mies.webdev.Service.Exception.WrongUsernameOrPasswordException;

import java.util.Optional;

@Path("/Autentication")
public class AutenticationResource {
    private final AutenticationService autenticationService;
    private final ClienteRepo clienteRepo;
    private final SessionRepo sessionRepo;

    public AutenticationResource(AutenticationService autenticationService, ClienteRepo clienteRepo, SessionRepo sessionRepo) {
        this.autenticationService = autenticationService;
        this.clienteRepo = clienteRepo;
        this.sessionRepo = sessionRepo;
    }

    @POST
    @Path("/Register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Cliente cliente) throws ClienteCreationException {
        autenticationService.register(cliente.getUsername(), cliente.getPassword(), cliente.getSedeLegale(), cliente.getpIva(), cliente.getEmail(), cliente.getTelefono(), cliente.getStato());
        return Response.ok().build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) throws SessionCreationException, WrongUsernameOrPasswordException {
        Optional<Cliente> maybeUtente = clienteRepo.findByUsername(request.getUsername());
        if (maybeUtente.isPresent()) {
            Optional<Sessione> maybeSessione = sessionRepo.getSessionByUserId(maybeUtente.get().getId());
            //Se l'utente ha già una sessione attiva
            if (maybeSessione.isPresent()) {
                throw new SessionCreationException("L'utente ha già una sessione attiva.");
            }
        }
        int sessione = autenticationService.login(request.getUsername(), request.getPassword());
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                .value(String.valueOf(sessione))
                .path("/")
                .build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_COOKIE") int sessionId) {
        autenticationService.logout(sessionId);
        NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE").path("/").build();
        return Response.ok()
                .cookie(sessionCookie)
                .build();
    }

}