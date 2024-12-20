package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.Cliente;
import miesgroup.mies.webdev.Rest.Model.UpdateUtente;
import miesgroup.mies.webdev.Service.ClienteService;
import miesgroup.mies.webdev.Service.SessionService;

import java.util.Map;

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
    public Response getCliente(@CookieParam("SESSION_COOKIE") int sessionId) {
        Cliente c = clienteService.getCliente(sessionService.trovaUtentebBySessione(sessionId));
        return Response.ok(c).build();
    }


    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCliente(@CookieParam("SESSION_COOKIE") int sessionId, Map<String, String> updateData) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);
        updateData.forEach((field, newValue) -> clienteService.updateCliente(idUtente, field, newValue));
        return Response.ok().build();
    }
}
