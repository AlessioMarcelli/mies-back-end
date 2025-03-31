package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.Sessione;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Repository.SessionRepo;
import miesgroup.mies.webdev.Rest.Model.ClienteResponse;
import miesgroup.mies.webdev.Rest.Model.LoginRequest;
import miesgroup.mies.webdev.Service.AutenticationService;
import miesgroup.mies.webdev.Service.ClienteService;
import miesgroup.mies.webdev.Service.Exception.ClienteCreationException;
import miesgroup.mies.webdev.Service.Exception.SessionCreationException;
import miesgroup.mies.webdev.Service.Exception.WrongUsernameOrPasswordException;
import miesgroup.mies.webdev.Service.SessionService;

import java.util.Optional;

@Path("/Autentication")
public class AutenticationResource {
    private final AutenticationService autenticationService;
    private final ClienteService clienteSevice;
    private final ClienteRepo clienteRepo;
    private final SessionRepo sessionRepo;
    private final SessionService sessionService;

    public AutenticationResource(AutenticationService autenticationService, ClienteService clienteSevice, ClienteRepo clienteRepo, SessionRepo sessionRepo, SessionService sessionService) {
        this.autenticationService = autenticationService;
        this.clienteSevice = clienteSevice;
        this.clienteRepo = clienteRepo;
        this.sessionRepo = sessionRepo;
        this.sessionService = sessionService;
    }

    @POST
    @Path("/Register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Cliente cliente) throws ClienteCreationException {
        autenticationService.register(cliente.getUsername(), cliente.getPassword(), cliente.getSedeLegale(), cliente.getpIva(), cliente.getEmail(), cliente.getTelefono(), cliente.getStato(), cliente.getTipologia());
        return Response.ok("utente registrato").build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        try {
            Optional<Cliente> maybeUtente = clienteRepo.findByUsername(request.getUsername());
            NewCookie sessionCookie = null;

            if (maybeUtente.isPresent()) {
                Optional<Sessione> maybeSessione = sessionRepo.getSessionByUserId(maybeUtente.get().getId());

                // Se l'utente ha già una sessione attiva, la invalidiamo
                if (maybeSessione.isPresent()) {
                    autenticationService.logout(maybeSessione.get().getId());

                    // Cookie che scade subito per invalidare quello esistente
                    sessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                            .value("") // Vuoto per invalidarlo
                            .path("/")
                            .maxAge(0) // Scade immediatamente
                            .build();
                }
            }

            // Ora creiamo una nuova sessione
            int sessione = autenticationService.login(request.getUsername(), request.getPassword());

            // Creiamo il nuovo session cookie
            NewCookie newSessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                    .value(String.valueOf(sessione))
                    .path("/")
                    .sameSite(NewCookie.SameSite.LAX)
                    .secure(false)
                    .httpOnly(true)
                    .build();

            return Response.ok()
                    .cookie(sessionCookie, newSessionCookie) // Passiamo entrambi per invalidare e creare
                    .build();

        } catch (SessionCreationException | WrongUsernameOrPasswordException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Login failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/logout")
    public Response logout(@CookieParam("SESSION_COOKIE") int sessionId) {
        try {
            autenticationService.logout(sessionId);
            NewCookie sessionCookie = new NewCookie.Builder("SESSION_COOKIE")
                    .path("/")
                    .build();
            return Response.ok()
                    .cookie(sessionCookie)
                    .build();
        } catch (Exception e) {
            // Log dell'errore (usa il logger del tuo progetto, qui ad esempio System.err)
            System.err.println("Errore durante il logout: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore durante il logout")
                    .build();
        }
    }

    @GET
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response check(@CookieParam("SESSION_COOKIE") int sessionId) {
        try {
            Integer sessione = sessionService.trovaUtentebBySessione(sessionId);
            if (sessione != null) {
                return Response.ok("Sessione presente").build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida")
                        .build();
            }
        } catch (Exception e) {
            System.err.println("Errore durante il check della sessione: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore interno durante il controllo della sessione")
                    .build();
        }
    }

    @GET
    @Path("/checkCategoria")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkCategoria(@CookieParam("SESSION_COOKIE") int sessionId) {
        try {
            Cliente c = sessionService.trovaUtenteCategoryBySessione(sessionId);
            if (c == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Cliente non trovato")
                        .build();
            } else {
                ClienteResponse response = clienteSevice.parseResponse(c);
                return Response.ok(response).build();
            }
        } catch (Exception e) {
            System.err.println("Errore durante il check della categoria: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore interno durante il controllo della categoria")
                    .build();
        }
    }


}