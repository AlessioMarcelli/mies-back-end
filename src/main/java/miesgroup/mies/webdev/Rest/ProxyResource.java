package miesgroup.mies.webdev.Rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import miesgroup.mies.webdev.Service.AzureADService;
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
    private final AzureADService azureADService;
    private static final String DATASET_ID_CONTROLLO = "82512a63-c371-49e5-96e1-987ec353857a";
    private static final String TABLE_NAME = "articoli";

    public ProxyResource(SessionController sessionController, AzureADService azureADService) {
        this.sessionController = sessionController;
        this.azureADService = azureADService;
    }

    @GET
    @Path("/pod")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPod() {
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
            String targetUrl = "http://energyportfolio.it:8081/pod/dati?session_id=" + sessionCookie;

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


    @GET
    @Path("/bollette")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBollette() {
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

    @GET
    @Path("/articoli")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inviaArticoliAPowerBI() {
        try {
            // 🔐 Recupero session ID
            String sessionCookie = sessionController.getSessionValue();
            if (sessionCookie == null || sessionCookie.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Missing SESSION_COOKIE\"}")
                        .build();
            }

            // 🌐 Chiamata all'API per ottenere i dati
            HttpClient client = HttpClient.newHttpClient();
            String targetUrl = "http://energyportfolio.it:8081/costo-articolo?session_id=" + sessionCookie;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Origin", "https://app.powerbi.com")
                    .header("X-Session-Id", sessionCookie)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Response Code: " + response.statusCode());
                System.out.println("Response Body: " + response.body());
                return Response.status(response.statusCode())
                        .entity("{\"error\":\"Errore dal server dati\"}")
                        .build();


            }

            // ✅ Riceviamo i dati come JSON (presumibilmente una lista di articoli)
            String rawJson = response.body();
            System.out.println("Dati ricevuti da energyportfolio: " + rawJson);

            // 🎯 Convertilo nel formato richiesto da Power BI
            String powerBIJson = wrapArticoliForPowerBI(rawJson); // funzione helper che vediamo sotto

            // 🔐 Ottieni token da AzureADService
            String token = azureADService.getPowerBIAccessToken();

            // 📤 Invia i dati a Power BI
            HttpRequest powerBIRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.powerbi.com/v1.0/myorg/datasets/" + DATASET_ID_CONTROLLO + "/tables/" + TABLE_NAME + "/rows"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(powerBIJson))
                    .build();

            HttpResponse<String> powerBIResponse = client.send(powerBIRequest, HttpResponse.BodyHandlers.ofString());

            if (powerBIResponse.statusCode() == 200) {
                // (opzionale) 🔄 Avvia il refresh del report
                refreshDataset(token, DATASET_ID_CONTROLLO); // solo se usi un dataset import/push

                return Response.ok("{\"status\":\"Dati inviati con successo a Power BI\"}").build();
            } else {
                return Response.status(powerBIResponse.statusCode())
                        .entity("{\"error\":\"Errore durante l'invio a Power BI: " + powerBIResponse.body() + "\"}")
                        .build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Errore generale: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private String wrapArticoliForPowerBI(String rawJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Presumo che rawJson sia un array di oggetti come:
        // [{ "categoria": "...", "descrizione": "...", "costo": 123.45 }, ...]
        JsonNode articoliArray = mapper.readTree(rawJson);

        ObjectNode root = mapper.createObjectNode();
        root.set("rows", articoliArray);

        return mapper.writeValueAsString(root);
    }

    public void refreshDataset(String accessToken, String datasetId) throws IOException, InterruptedException {
        String url = "https://api.powerbi.com/v1.0/myorg/datasets/" + datasetId + "/refreshes";

        HttpClient client = HttpClient.newHttpClient(); // Definizione del client HTTP

        // Creazione della richiesta HTTP
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.noBody()) // corpo vuoto
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 202) {
            System.out.println("✅ Refresh del dataset avviato correttamente.");
        } else {
            System.err.println("❌ Errore nel refresh dataset: " + response.body());
        }
    }


}

