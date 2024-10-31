package miesgroup.mies.webdev.Rest.Model;

import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import java.io.InputStream;

public class FormData {

    @FormParam("file")
    @PartType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    private InputStream file;

    @FormParam("fileName")
    @PartType("text/plain")
    private String fileName;

    // Getter e Setter
    public InputStream getFile() {
        return file;
    }

    public void setFile(InputStream file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
