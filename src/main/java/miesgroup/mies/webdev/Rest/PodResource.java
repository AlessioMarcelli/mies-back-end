package miesgroup.mies.webdev.Rest;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Rest.Model.UpdatePodRequest;
import miesgroup.mies.webdev.Service.PodService;

import java.util.ArrayList;

@Path("/pod")
public class PodResource {
    private final PodService podService;

    public PodResource(PodService podService) {
        this.podService = podService;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Pod> allPod(@CookieParam("SESSION_COOKIE") int id_sessione) {
        return podService.tutti(id_sessione);
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

}
