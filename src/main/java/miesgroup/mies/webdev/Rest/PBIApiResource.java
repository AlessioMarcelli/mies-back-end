package miesgroup.mies.webdev.Rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Service.AzureADService;

import java.util.Map;

@Path("/api/pbitoken")
public class PBIApiResource {

    @Inject
    AzureADService azureADService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPBIAccessToken() {
        try {
            String accessToken = azureADService.getPowerBIAccessToken();
            System.out.println("Access Token: " + accessToken);
            return Response.ok(Map.of("token", accessToken)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Failed to get Power BI access token", "details", e.getMessage()))
                    .build();
        }
    }
}
