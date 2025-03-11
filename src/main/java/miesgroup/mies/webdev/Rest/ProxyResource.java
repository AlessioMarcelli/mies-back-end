package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;

@Path("/proxy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProxyResource {

    @GET
    @Path("/pod")
    public Response getPod(@CookieParam("SESSION_COOKIE") Integer sessionCookie) {
        try {
            // Verifica se il cookie è presente
            if (sessionCookie == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing SESSION_COOKIE").build();
            }

            // Creazione client HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://energyportfolio.it:8081/pod"))
                    .header("Origin", "https://app.powerbi.com")
                    .header("Cookie", "SESSION_COOKIE=" + sessionCookie) // Inoltra il cookie
                    .GET()
                    .build();

            // Esecuzione richiesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Restituisci la risposta originale
            return Response.status(response.statusCode())
                    .entity(response.body())
                    .build();

        } catch (IOException | InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nel proxy: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/bollette")
    public Response getBollette(@CookieParam("SESSION_COOKIE") Integer sessionCookie) {
        try {
            // Verifica se il cookie è presente
            if (sessionCookie == null) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Missing SESSION_COOKIE").build();
            }

            // Creazione client HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://energyportfolio.it:8081/files/dati"))
                    .header("Origin", "https://app.powerbi.com")
                    .header("Cookie", "SESSION_COOKIE=" + sessionCookie) // Inoltra il cookie
                    .GET()
                    .build();

            // Esecuzione richiesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Restituisci la risposta originale
            return Response.status(response.statusCode())
                    .entity(response.body())
                    .build();

        } catch (IOException | InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Errore nel proxy: " + e.getMessage())
                    .build();
        }
    }
}
