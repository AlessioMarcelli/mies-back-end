package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import miesgroup.mies.webdev.Service.SessionController;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;

@Path("/proxy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProxyResource {
    private final SessionController sessionController;

    public ProxyResource(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @GET
    @Path("/pod")
    @Produces("application/json")
    public Response getPod(@CookieParam("SESSION_COOKIE") Integer sessionCookie) {
        try {
            // 🔴 Controllo che il cookie sia presente
            if (sessionCookie == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Missing SESSION_COOKIE")
                        .build();
            }

            // 🔵 Converti il cookie da Integer a Stringa
            String cookieString = String.valueOf(sessionCookie);

            // 🔵 Creazione client HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://energyportfolio.it:8081/pod"))
                    .header("Origin", "https://app.powerbi.com") // Abilita CORS
                    .header("Cookie", "SESSION_COOKIE=" + cookieString) // Inoltra il cookie convertito in stringa
                    .header("User-Agent", "Quarkus-Proxy") // Identificazione della richiesta
                    .GET()
                    .build();

            // 🟢 Esecuzione richiesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 🟢 Restituisci la risposta originale
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatiProxy() {
        try {
            // Recuperiamo il valore del cookie dalla classe SessionControl
            String sessionCookie = sessionController.getSessionValue();

            System.out.println("Session ID: " + sessionCookie);

            // Se il valore è null o vuoto, restituiamo un errore
            if (sessionCookie == null || sessionCookie.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Missing SESSION_COOKIE porco dio\"}")
                        .build();
            }

            // Creazione client HTTP
            HttpClient client = HttpClient.newHttpClient();

            // URL della vera API (con session ID come header)
            String targetUrl = "http://energyportfolio.it:8081/files/dati?session_id=" + sessionCookie;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Origin", "https://app.powerbi.com") // Aggiunto per compatibilità Power BI
                    .header("X-Session-Id", sessionCookie) // Passiamo il valore come HEADER
                    .GET()
                    .build();

            // Eseguiamo la richiesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Log per debugging
            System.out.println("Request to: " + targetUrl);
            System.out.println("Session ID: " + sessionCookie);
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            // Restituiamo la risposta della vera API
            return Response.status(response.statusCode())
                    .entity(response.body())
                    .build();

        } catch (IOException | InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore nel proxy: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
