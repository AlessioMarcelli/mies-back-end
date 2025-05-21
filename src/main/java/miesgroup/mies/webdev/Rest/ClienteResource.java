package miesgroup.mies.webdev.Rest;

import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.CostoEnergia;
import miesgroup.mies.webdev.Service.ClienteService;
import miesgroup.mies.webdev.Service.CostoEnergiaService;
import miesgroup.mies.webdev.Service.SessionService;

import java.util.List;
import java.util.Map;

@Path("/cliente")
public class ClienteResource {

    private final ClienteService clienteService;
    private final SessionService sessionService;
    private final CostoEnergiaService costoEnergiaService;

    public ClienteResource(ClienteService clienteService, SessionService sessionService, CostoEnergiaService costoEnergiaService) {
        this.clienteService = clienteService;
        this.sessionService = sessionService;
        this.costoEnergiaService = costoEnergiaService;
    }

    // Recupera il cliente basato sul sessionId
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCliente(@CookieParam("SESSION_COOKIE") int sessionId) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);
        Cliente cliente = clienteService.getCliente(idUtente);

        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Cliente non trovato")
                    .build();
        }

        return Response.ok(cliente).build();
    }

    // Aggiorna le informazioni del cliente
    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCliente(@CookieParam("SESSION_COOKIE") int sessionId, Map<String, String> updateData) {
        int idUtente = sessionService.trovaUtentebBySessione(sessionId);

        if (idUtente == 0) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Sessione non valida")
                    .build();
        }

        for (Map.Entry<String, String> entry : updateData.entrySet()) {
            String field = entry.getKey();
            String newValue = entry.getValue();

            boolean isUpdated = clienteService.updateCliente(idUtente, field, newValue);
            if (!isUpdated) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Aggiornamento fallito per il campo: " + field)
                        .build();
            }
        }

        return Response.ok().build();
    }

    @GET
    @Path("/costi-energia")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCostiEnergia(@CookieParam("SESSION_COOKIE") int sessionId) {
        try {
            Integer idUtente = sessionService.trovaUtentebBySessione(sessionId);
            if (idUtente == null || idUtente == 0) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida")
                        .build();
            }

            List<CostoEnergia> costi = costoEnergiaService.getCostiEnergia(idUtente);
            if (costi == null || costi.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Nessun costo trovato per il cliente")
                        .build();
            }

            return Response.ok(costi).build();
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage()); // Log dell'errore, in ambiente di produzione usa un logger
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore interno del server: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/costi-energia/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response insertCostoEnergia(@CookieParam("SESSION_COOKIE") int sessionId, List<CostoEnergia> costiEnergia) {
        try {
            Integer idUtente = sessionService.trovaUtentebBySessione(sessionId);
            if (idUtente == null || idUtente == 0) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Sessione non valida")
                        .build();
            }

            Cliente cliente = clienteService.getCliente(idUtente);
            if (cliente == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Cliente non trovato")
                        .build();
            }

            for (CostoEnergia costo : costiEnergia) {
                // Associa il cliente al costo
                costo.setCliente(cliente);

                // Verifica che i campi obbligatori siano presenti
                if (costo.getNomeCosto() == null || costo.getCostoEuro() == null) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Nome costo e costo in euro sono obbligatori per ogni elemento")
                            .build();
                }

                // Persisti o aggiorna il costo
                costoEnergiaService.persistOrUpdateCostoEnergia(costo);
            }

            return Response.ok().build();
        } catch (Exception e) {
            System.out.println(" Errore: " + e.getMessage()); // Log dell'errore
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore interno del server: " + e.getMessage())
                    .build();
        }
    }


}
