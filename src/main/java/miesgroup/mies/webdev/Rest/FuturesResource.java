package miesgroup.mies.webdev.Rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.Future;
import miesgroup.mies.webdev.Service.FuturesService;

import java.sql.SQLException;
import java.util.List;

@Path("/futures")
public class FuturesResource {
    private final FuturesService futuresService;

    // Iniezione del servizio per ottenere i dati
    @Inject
    public FuturesResource(FuturesService futuresService) {
        this.futuresService = futuresService;
    }

    // Endpoint per ottenere i dati dei futures in base alla selezione dell'utente
    @GET
    @Path("/FuturesData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuturesData(
            @QueryParam("date") String date,      // La data selezionata dall'utente
            @QueryParam("type") String type) {    // Tipo di future: "year", "quarter" o "month"

        // Chiamata al servizio che interroga il database in base ai parametri
        List<Future> futuresList = futuresService.getFutures(date, type);

        if (futuresList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dati non trovati per la selezione fornita.").build();
        }

        return Response.ok(futuresList).build();
    }

    @GET
    @Path("/futuresYear")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuturesYear(
            @QueryParam("date") String date,      // La data selezionata dall'utente
            @QueryParam("type") String type) {    // Tipo di future: "year", "quarter" o "month"

        // Chiamata al servizio che interroga il database in base ai parametri
        List<Future> futuresList = futuresService.getFuturesYear(date);

        if (futuresList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dati non trovati per la selezione fornita.").build();
        }

        return Response.ok(futuresList).build();
    }

    @GET
    @Path("/futuresQuarter")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuturesQuarter(
            @QueryParam("date") String date,      // La data selezionata dall'utente
            @QueryParam("type") String type) {    // Tipo di future: "year", "quarter" o "month"

        // Chiamata al servizio che interroga il database in base ai parametri
        List<Future> futuresList = futuresService.getFuturesQuarter(date);

        if (futuresList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dati non trovati per la selezione fornita.").build();
        }

        return Response.ok(futuresList).build();
    }

    @GET
    @Path("/futuresYear")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFuturesMonth(
            @QueryParam("date") String date,      // La data selezionata dall'utente
            @QueryParam("type") String type) {    // Tipo di future: "year", "quarter" o "month"

        // Chiamata al servizio che interroga il database in base ai parametri
        List<Future> futuresList = futuresService.getFuturesYear(date);

        if (futuresList.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Dati non trovati per la selezione fornita.").build();
        }

        return Response.ok(futuresList).build();
    }




    @GET
    @Path("/last-date")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLastDate() throws SQLException {
        // Chiamata al servizio per ottenere l'ultima data
        String lastDate = futuresService.getLastDate();

        if (lastDate == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Ultima data non trovata.").build();
        }

        return Response.ok(lastDate).build();
    }
}