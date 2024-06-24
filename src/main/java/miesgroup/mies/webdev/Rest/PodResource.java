package miesgroup.mies.webdev.Rest;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Pod;
import miesgroup.mies.webdev.Service.PodService;

import java.util.ArrayList;
import java.util.List;

@Path("/pods")
public class PodResource {
    private final PodService podService;

    public PodResource(PodService podService) {
        this.podService = podService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPod(Pod pod) {
        podService.createPod(pod.getId(), pod.getTensione_Alimentazione(), pod.getPotenza_Impegnata(), pod.getPotenza_Disponibile(), pod.getId_utente());
        return Response.ok().build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
public ArrayList<Pod> allPod(){
        return podService.tutti();
    }


}
