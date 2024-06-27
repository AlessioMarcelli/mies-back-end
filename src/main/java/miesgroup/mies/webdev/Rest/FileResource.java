package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Rest.Model.FileUploadForm;
import miesgroup.mies.webdev.Service.FileService;
import miesgroup.mies.webdev.Service.PodService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import java.sql.SQLException;
import org.w3c.dom.Document;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Path("/files")
public class FileResource {

    private final FileService fileService;
    private final PodService podService;

    public FileResource(FileService fileService, PodService podService) {
        this.fileService = fileService;
        this.podService = podService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FileUploadForm form) {
        try {
            fileService.saveFile(form.getFileName(), form.getFileData());
            return Response.ok().build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Database error: " + e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid input: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred: " + e.getMessage()).build();
        }
    }

    //estrazione dei dati richiesti dlla bolletta e creazione del pod in caso non esista già
    @GET
    @Path("/{id}/estrazione")
    @Produces(MediaType.APPLICATION_XML)
    public Response convertPdfToXml(@PathParam("id") int id,@CookieParam("SESSION_COOKIE") int id_utente) {
        try {
            PDFFile pdfFile = fileService.getFile(id);
            if (pdfFile == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Converti il PDF in XML
            Document xmlDocument = fileService.convertPdfToXml(pdfFile.getFile_Data());

            // Converti il documento XML in una stringa
            String xmlString = fileService.convertDocumentToString(xmlDocument);
            System.out.println("XML Content: " + xmlString);

            // Estrai i valori dal documento XML
            byte[] xmlData = xmlString.getBytes();
            fileService.extractValuesFromXml(xmlData);
            podService.extractValuesFromXml(xmlData,id_utente);
            // Restituisci i valori estratti
            return Response.ok("dati caricati con successo").build();
        } catch (IOException | ParserConfigurationException | TransformerException | SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

}