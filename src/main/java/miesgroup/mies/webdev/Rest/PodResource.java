package miesgroup.mies.webdev.Rest;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Service.PodService;

import java.util.ArrayList;

@Path("/pods")
public class PodResource {
    private final PodService podService;

    public PodResource(PodService podService) {
        this.podService = podService;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<Pod> allPod(@CookieParam("SESSION_COOKIE") int id_utente) {
        return podService.tutti(id_utente);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Pod getPod(@PathParam("id") String id, @CookieParam("SESSION_COOKIE") int id_utente) {
        return podService.getPod(id, id_utente);
    }

}
