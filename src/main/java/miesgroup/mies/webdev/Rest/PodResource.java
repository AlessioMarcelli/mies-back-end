package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.PDFFile;
import miesgroup.mies.webdev.Model.Pod;
import miesgroup.mies.webdev.Rest.Model.PodResponse;
import miesgroup.mies.webdev.Rest.Model.UpdatePodRequest;
import miesgroup.mies.webdev.Service.PodService;

import java.util.List;

@Path("/pod")
public class PodResource {
    private final PodService podService;

    public PodResource(PodService podService) {
        this.podService = podService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<PodResponse> allPod(@CookieParam("SESSION_COOKIE") Integer sessionId) {
        return podService.tutti(sessionId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dati")
    public List<PodResponse> allPodProxy(@QueryParam("session_id") Integer sessionId) {
        return podService.tutti(sessionId);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Pod getPod(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") int id_sessione) {
        return podService.getPod(id, id_sessione);
    }

    @PUT
    @Path("/sedeNazione")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePod(UpdatePodRequest request, @CookieParam("SESSION_COOKIE") int id_sessione) {
        podService.addSedeNazione(request.getIdPod(), request.getSede(), request.getNazione(), id_sessione);
    }

    @PUT
    @Path("/modifica-sede-nazione")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updatePodSedeNazione(UpdatePodRequest request, @CookieParam("SESSION_COOKIE") int id_sessione) {
        podService.modificaSedeNazione(request.getIdPod(), request.getSede(), request.getNazione(), id_sessione);
    }

    @GET
    @Path("/bollette")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PDFFile> getBollette(@CookieParam("SESSION_COOKIE") int id_sessione) {
        List<Pod> elencoPod = podService.findPodByIdUser(id_sessione);
        return podService.getBollette(elencoPod);
    }

    @PUT
    @Path("/spread")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSpread(UpdatePodRequest request, @CookieParam("SESSION_COOKIE") int id_sessione) {
        podService.addSpread(request.getIdPod(), request.getSpread(), id_sessione);
        return Response.ok().build();
    }

}