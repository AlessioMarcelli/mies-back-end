package miesgroup.mies.webdev.Rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Rest.Model.FileUploadForm;
import miesgroup.mies.webdev.Service.FileService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

@Path("/files")
public class FileResource {

    private final FileService fileService;

    public FileResource(FileService fileService) {
        this.fileService = fileService;
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FileUploadForm form) throws SQLException {
        fileService.saveFile(form.getFileName(), form.getFileData());
        return Response.ok().build();
    }

    @GET
    @Path("/{id}/to-xml")
    @Produces(MediaType.APPLICATION_XML)
    public Response convertPdfToXml(@PathParam("id") int id) {
        try {
            PDFFile pdfFile = fileService.getFile(id);
            if (pdfFile == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // Converti il PDF in XML
            Document xmlDocument = fileService.convertPdfToXml(pdfFile.getFile_Data());

            // Converti il documento XML in una stringa
            String xmlString = fileService.convertDocumentToString(xmlDocument);

            // Estrai i valori dal documento XML
            byte[] xmlData = xmlString.getBytes();
            ArrayList<Double> extractedValues = fileService.extractValuesFromXml(xmlData);

           // Restituisci i valori estratti
            return Response.ok().build();
        } catch (IOException | ParserConfigurationException | TransformerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }



}
