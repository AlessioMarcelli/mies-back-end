package miesgroup.mies.webdev.Rest;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.Costi;
import miesgroup.mies.webdev.Rest.Model.CostiDTO;
import miesgroup.mies.webdev.Rest.Model.FormData;
import miesgroup.mies.webdev.Service.CostiService;
import miesgroup.mies.webdev.Service.SessionService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/costi")
public class CostiResource {
    private final CostiService costiService;
    private final SessionService sessionService;

    public CostiResource(CostiService costiService, SessionService sessionService) {
        this.costiService = costiService;
        this.sessionService = sessionService;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCosti(
            @CookieParam("SESSION_COOKIE") Integer idSessione,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {

        Cliente c = sessionService.trovaUtenteCategoryBySessione(idSessione);
        if (!"Admin".equals(c.getTipologia())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        // Calcolo offset
        int offset = page * size;

        // Query con paginazione
        PanacheQuery<Costi> query = costiService.getQueryAllCosti(idSessione);
        List<Costi> paginatedList = query
                .range(offset, offset + size - 1)
                .list();

        // Opzionale: includere anche info su totale pagine, elementi, pagina corrente, ecc.
        long total = query.count();

        Map<String, Object> response = new HashMap<>();
        response.put("contenuto", paginatedList);
        response.put("pagina", page);
        response.put("dimensione", size);
        response.put("totaleElementi", total);
        response.put("totalePagine", (int) Math.ceil((double) total / size));

        return Response.ok(response).build();
    }


    @POST
    @Path("/aggiungi")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCosto(Costi costo) throws SQLException {
        boolean verifica = costiService.createCosto(costo.getDescrizione(), costo.getCategoria(), costo.getUnitaMisura(), costo.getTrimestre(), costo.getAnno(), costo.getCosto(), costo.getIntervalloPotenza(), costo.getClasseAgevolazione());
        if (!verifica) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/{IntervalloPotenza}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Costi getSum(@PathParam("IntervalloPotenza") String intervalloPotenza) throws SQLException {
        return costiService.getSum(intervalloPotenza);
    }

    @Path("/delete/{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCosto(@CookieParam("SESSION_COOKIE") int idSessione, @PathParam("id") int id) throws SQLException {
        Cliente c = sessionService.trovaUtenteCategoryBySessione(idSessione);
        if (c.getTipologia().equals("Admin")) {
            costiService.deleteCosto(id);
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadExcelFile(@MultipartForm FormData formData) {
        try {
            InputStream excelInputStream = formData.getFile();
            costiService.processExcelFile(excelInputStream);
            return Response.ok("File elaborato con successo").build();
        } catch (Exception e) {
            return Response.serverError().entity("Errore: " + e.getMessage()).build();
        }
    }


    @Path("/update")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadCosto(Costi costo) {
        boolean verifica = costiService.updateCosto(costo.getId(), costo.getDescrizione(), costo.getCategoria(), costo.getUnitaMisura(), costo.getTrimestre(), costo.getAnno(), costo.getCosto(), costo.getIntervalloPotenza(), costo.getClasseAgevolazione());
        if (!verifica) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok("Update avvenuto con successo").build();
    }

    @Path("/downloadExcel")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response downloadExcel() {
        try {
            ByteArrayOutputStream out = costiService.generateExcelFile();
            byte[] excelData = out.toByteArray(); // Salva i dati prima di chiudere il flusso
            return Response.ok(excelData)
                    .header("Content-Disposition", "attachment; filename=costi.xlsx")
                    .build();
        } catch (Exception e) {
            return Response.serverError().entity("Errore nella generazione del file Excel: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/delete-ids")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCostiByIds(List<Long> ids, @CookieParam("SESSION_COOKIE") int idSessione) {
        if (ids == null || ids.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Lista di ID vuota o nulla")
                    .build();
        }

        // Verifica se l'utente ha i permessi per eliminare
        Cliente c = sessionService.trovaUtenteCategoryBySessione(idSessione);
        if (!"Admin".equals(c.getTipologia())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        long deletedCount = costiService.deleteIds(ids);
        return Response.ok(Map.of("deleted", deletedCount)).build();
    }

    @GET
    @Path("/filtrati")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCostiFiltrati(
            @QueryParam("categoria") Optional<String> categoria,
            @QueryParam("anno") Optional<String> anno,
            @QueryParam("annoRiferimento") Optional<String> annoRif,
            @QueryParam("intervalloPotenza") Optional<String> intervalloPotenza,
            @QueryParam("id") Optional<Integer> id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        // Recupera la lista dei costi filtrati con paginazione
        List<CostiDTO> dtoList = costiService.getCostiFiltrati(categoria, anno, annoRif, intervalloPotenza, id, page, size);

        // Recupera il totale degli elementi filtrati
        long total = costiService.countCostiFiltrati(categoria, anno, annoRif, intervalloPotenza);
        int totalPages = (int) Math.ceil((double) total / size);

        Map<String, Object> response = new HashMap<>();
        response.put("contenuto", dtoList);
        response.put("pagina", page);
        response.put("dimensione", size);
        response.put("totaleElementi", total);
        response.put("totalePagine", totalPages);

        return Response.ok(response).build();
    }

}