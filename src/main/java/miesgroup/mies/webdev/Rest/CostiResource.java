package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.Costi;
import miesgroup.mies.webdev.Rest.Model.FormData;
import miesgroup.mies.webdev.Service.CostiService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.InputStream;
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
        costiService.createCosto(costo.getDescrizione(), costo.getCategoria(), costo.getUnitaMisura(), costo.getTrimestre(), costo.getAnno(), costo.getCosto(), costo.getIntervalloPotenza(), costo.getClasseAgevolazione());
        return Response.ok().build();
    }


    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadExcelFile(@MultipartForm FormData formData) throws Exception {
        InputStream excelInputStream = formData.getFile();
        costiService.readExcelFile(excelInputStream);
        return Response.ok("File caricato con successo").build();
    }

}
