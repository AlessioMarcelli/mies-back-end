package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Model.PDFFile;
import miesgroup.mies.webdev.Rest.Model.FileUploadForm;
import miesgroup.mies.webdev.Service.FileService;
import miesgroup.mies.webdev.Service.PodService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.sql.SQLException;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Map;

@Path("/files")
public class BollettaResource {

    private final FileService fileService;
    private final PodService podService;

    public BollettaResource(FileService fileService, PodService podService) {
        this.fileService = fileService;
        this.podService = podService;

    }

    @Path("/xml/{id}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getXml(@PathParam("id") int id) throws IOException, ParserConfigurationException {
        byte[] xmlData = fileService.getXmlData(id);
        Document xmlDoc = fileService.convertPdfToXml(xmlData);
        return Response.ok(xmlDoc, MediaType.APPLICATION_XML).build();
    }


    @Path("/upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response uploadAndProcessFileA2A(@MultipartForm FileUploadForm form, @CookieParam("SESSION_COOKIE") int idSessione) {
        try {
            // 1. Salva il file caricato e ottieni l'ID associato
            int idFile = fileService.saveFile(form.getFileName(), form.getFileData());

            // 2. Converte il file PDF caricato in un documento XML
            Document xmlDocument = fileService.convertPdfToXml(form.getFileData());

            // 3. Converte il documento XML in una stringa
            String xmlString = fileService.convertDocumentToString(xmlDocument);
            byte[] xmlData = xmlString.getBytes();

            // 4. Estrae l'ID del POD dal documento XML usando i dati della sessione
            String idPod = podService.extractValuesFromXml(xmlData, idSessione);

            // Verifica che l'ID del POD sia stato estratto correttamente
            if (idPod == null || idPod.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Impossibile estrarre l'ID del POD dal documento XML.")
                        .build();
            }

            // 5. Estrai i dati della bolletta dal documento XML e inseriscili nel database
            String nomeB = fileService.extractValuesFromXmlA2A(xmlData, idPod);
            if (nomeB == null || nomeB.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Impossibile estrarre i dati della bolletta dal documento XML.")
                        .build();
            }

            // 6. effettua i vari calcoli per verificare la correttezza dei dati
            fileService.verificaA2APiuMesi(nomeB);

            // 7. verifica di possibili ricalcoli
            fileService.controlloRicalcoliInBolletta(xmlData, idPod, nomeB, idSessione);

            // 8. Associa l'ID del POD con l'ID del file caricato
            fileService.abbinaPod(idFile, idPod);

            // 9. Restituisci una risposta di successo
            return Response.status(Response.Status.OK)
                    .entity("<message>File caricato e processato con successo.</message>")
                    .build();
        } catch (IOException | ParserConfigurationException | TransformerException | SQLException e) {
            // Gestione di errori specifici relativi al salvataggio, alla conversione o al database
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<error>" + e.getMessage() + "</error>")
                    .build();
        } catch (IllegalArgumentException e) {
            // Gestione di input non validi
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("<error>Input non valido: " + e.getMessage() + "</error>")
                    .build();
        } catch (Exception e) {
            // Gestione di errori generici o imprevisti
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<error>Si è verificato un errore inaspettato: " + e.getMessage() + "</error>")
                    .build();
        }
    }

    @Path("/dati")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDati(@QueryParam("session_id") Integer sessionId) {
        try {
            if (sessionId == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\":\"Missing session_id\"}")
                        .build();
            }

            return Response.ok(fileService.getDati(sessionId)).build();
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid session_id format\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }


    @GET
    @Path("/{id}/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("id") int id) {
        PDFFile pdfFile = fileService.getFile(id); // Fetch the file from the database
        if (pdfFile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Fetch the file data and the file name
        byte[] fileData = pdfFile.getFileData();
        String fileName = pdfFile.getFileName();

        return Response.ok(fileData, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }

    @Path("/env")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getEnvVars() {
        return Map.of(
                "MAILER_HOST", System.getenv("MAILER_HOST"),
                "MAILER_PORT", System.getenv("MAILER_PORT"),
                "MAILER_PASSWORD", System.getenv("MAILER_PASSWORD")
        );

    }

}