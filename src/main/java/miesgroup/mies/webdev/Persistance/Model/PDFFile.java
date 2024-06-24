package miesgroup.mies.webdev.Persistance.Model;


public class PDFFile {

    private int id_File;

    private String File_Name;

    private byte[] file_Data;

    // Getters and setters
    public int getId_File() {
        return id_File;
    }

    public void setId_File(int id_File) {
        this.id_File = id_File;
    }

    public String getFile_Name() {
        return File_Name;
    }

    public void setFile_Name(String file_Name) {
        this.File_Name = file_Name;
    }

    public byte[] getFile_Data() {
        return file_Data;
    }

    public void setFile_Data(byte[] file_Data) {
        this.file_Data = file_Data;
    }
}
