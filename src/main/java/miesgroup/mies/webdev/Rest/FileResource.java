package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Repository.SessionRepo;
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

    public FileResource(FileService fileService, PodService podService, SessionRepo sessionRepo) {
        this.fileService = fileService;
        this.podService = podService;
    }

    @Path("/upload")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response uploadAndProcessFile(@MultipartForm FileUploadForm form, @CookieParam("SESSION_COOKIE") int id_sessione) {
        try {
            // Salva il file caricato
            int idFile = fileService.saveFile(form.getFileName(), form.getFileData());
            // Converti il PDF in XML
            Document xmlDocument = fileService.convertPdfToXml(form.getFileData());
            // Converti il documento XML in una stringa
            String xmlString = fileService.convertDocumentToString(xmlDocument);
            // Estrai i valori dal documento XML
            byte[] xmlData = xmlString.getBytes();
            String idPod = podService.extractValuesFromXml(xmlData, id_sessione); // inserisce pod
            fileService.extractValuesFromXml(xmlData, idPod); // Inserisce bolletta
            fileService.abbinaPod(idFile, idPod);
            // Restituisci i valori estratti
            return Response.ok("File caricato e dati salvati correttamente").build();
        } catch (IOException | ParserConfigurationException | TransformerException | SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid input: " + e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred: " + e.getMessage()).build();
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
        byte[] fileData = pdfFile.getFile_Data();
        String fileName = pdfFile.getFile_Name();

        return Response.ok(fileData, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }



}

