package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Service.AzureADService;

import java.util.Map;

@Path("/api/pbitoken")
public class PBIApiResource {

    private final AzureADService azureADService;

    public PBIApiResource(AzureADService azureADService) {
        this.azureADService = azureADService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPBIAccessToken() {
        String accessToken = azureADService.getPowerBIAccessToken();
        return Response.ok(Map.of("token", accessToken)).build();
    }
}
