package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Service.CostiService;

import java.sql.SQLException;
import java.util.ArrayList;

@Path("/costi")
public class CostiResource {
    private final CostiService costiService;

    public CostiResource(CostiService costiService) {
        this.costiService = costiService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Costi> getCosti() throws SQLException {
        return costiService.getAllCosti();
    }

    @POST
    @Path("/aggiungi")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCosto(Costi costo) throws SQLException {
        costiService.createCosto(costo.getDescrizione(), costo.getCategoria(), costo.getUnitaMisura(), costo.getTrimestre(), costo.getAnno(), costo.getCosto());
        return Response.ok().build();
    }
}
