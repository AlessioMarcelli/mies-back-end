package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Rest.Model.UpdateUtente;
import miesgroup.mies.webdev.Service.ClienteService;
import miesgroup.mies.webdev.Service.SessionService;

@Path("/cliente")
public class ClienteResource {
    private final ClienteService clienteService;
    private final SessionService sessionService;

    public ClienteResource(ClienteService clienteService, SessionService sessionService) {
        this.clienteService = clienteService;
        this.sessionService = sessionService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Cliente getCliente(@CookieParam("SESSION_COOCKIE") int sessionId) {
        return clienteService.getCliente(sessionService.trovaUtentebBySessione(sessionId));
    }

    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UpdateUtente updateUtente (@CookieParam("SESSION_COOCKIE") int sessionId, UpdateUtente updateUtente) {
        clienteService.updateUtente(sessionService.trovaUtentebBySessione(sessionId), updateUtente.getSedeLegale(), updateUtente.getpIva(), updateUtente.getTelefono(), updateUtente.getEmail(), updateUtente.getStato(), updateUtente.getClasseAgevolazione());
        return updateUtente;
    }

}
