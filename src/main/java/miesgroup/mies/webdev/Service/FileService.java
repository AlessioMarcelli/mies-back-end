package miesgroup.mies.webdev.Service;//package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Repository.BollettaRepo;
import miesgroup.mies.webdev.Persistance.Repository.FileRepo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

@ApplicationScoped
public class FileService {

    private final FileRepo fileRepo;
    private final BollettaRepo bollettaRepo;

    public FileService(FileRepo fileRepo, BollettaRepo bollettaRepo) {
        this.fileRepo = fileRepo;
        this.bollettaRepo = bollettaRepo;
    }

    public void saveFile(String fileName, byte[] fileData) throws SQLException {
        PDFFile pdfFile = new PDFFile();
        pdfFile.setFile_Name(fileName);
        pdfFile.setFile_Data(fileData);
        fileRepo.insert(pdfFile);
    }

    public PDFFile getFile(int id) {
        return fileRepo.find(id);
    }

    //CONVERTI FILE IN XML
    public Document convertPdfToXml(byte[] pdfData) throws IOException, ParserConfigurationException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfData))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document xmlDocument = documentBuilder.newDocument();

            Element rootElement = xmlDocument.createElement("PDFContent");
            xmlDocument.appendChild(rootElement);


            String[] lines = text.split("\\r?\\n");
            for (String line : lines) {
                Element lineElement = xmlDocument.createElement("Line");
                lineElement.appendChild(xmlDocument.createTextNode(line));
                rootElement.appendChild(lineElement);
            }

            return xmlDocument;
        }
    }

    public String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }


    //ESTRAI VALORI DA XML
    public ArrayList<Double> extractValuesFromXml(byte[] xmlData) {
        ArrayList<Double> extractedValues = new ArrayList<>();
        int fasciaOrariaCount = 0; // Contatore per "Fascia oraria"
        int spesaMateriaEnergiaCount = 0; // Contatore per "SPESA PER LA MATERIA ENERGIA"
        int spesaTrasportoGestioneContatoreCount = 0; // Contatore per "SPESA PER IL TRASPORTO E LA GESTIONE DEL CONTATORE"
        int spesaOneriSistemaCount = 0; // Contatore per "SPESA PER ONERI DI SISTEMA"
        int totaleImposteCount = 0; // Contatore per "TOTALE IMPOSTE"

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            InputStream is = new ByteArrayInputStream(xmlData);
            Document document = builder.parse(is);

            NodeList lineNodes = document.getElementsByTagName("Line");

            for (int i = 0; i < lineNodes.getLength(); i++) {
                Node lineNode = lineNodes.item(i);
                if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element lineElement = (Element) lineNode;
                    String lineText = lineElement.getTextContent();

                    // Debug: stampa il contenuto di lineText
                    System.out.println("Processing line: " + lineText);

                    if (lineText.contains("Fascia oraria") && fasciaOrariaCount < 8) {
                        fasciaOrariaCount++;
                        Double value = extractValueFromLine(lineText);
                        if (value != null) {
                            extractedValues.add(value);
                        }
                    }

                    if (lineText.contains("SPESA PER LA MATERIA ENERGIA") && spesaMateriaEnergiaCount < 2) {
                        spesaMateriaEnergiaCount++;
                        Double value = extractValueFromLine(lineText);
                        if (value != null) {
                            extractedValues.add(value);
                            System.out.println("Extracted value (SPESA PER LA MATERIA ENERGIA): " + value);
                        }
                    }

                    if (lineText.contains("SPESA PER IL TRASPORTO E LA GESTIONE DEL CONTATORE") && spesaTrasportoGestioneContatoreCount < 2) {
                        spesaTrasportoGestioneContatoreCount++;
                        Double value = extractValueFromLine(lineText);
                        if (value != null) {
                            extractedValues.add(value);
                            System.out.println("Extracted value (SPESA PER IL TRASPORTO E LA GESTIONE DEL CONTATORE): " + value);
                        }
                    }

                    if (lineText.contains("SPESA PER ONERI DI SISTEMA") && spesaOneriSistemaCount < 2) {
                        spesaOneriSistemaCount++;
                        Double value = extractValueFromLine(lineText);
                        if (value != null) {
                            extractedValues.add(value);
                            System.out.println("Extracted value (SPESA PER ONERI DI SISTEMA): " + value);
                        }
                    }

                    if (lineText.contains("TOTALE IMPOSTE") && totaleImposteCount < 2) {
                        totaleImposteCount++;
                        Double value = extractValueFromLine(lineText);
                        if (value != null) {
                            extractedValues.add(value);
                            System.out.println("Extracted value (TOTALE IMPOSTE): " + value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extractedValues;
    }

    private static Double extractValueFromLine(String lineText) {
        try {
            // Aggiungi log per il debug
            System.out.println("Extracting value from line: " + lineText);

            // Rimuovi i valori in formato data
            String regexDateAtStart = "^(\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\s+){1,2}";
            String lineTextWithoutDate1 = lineText.replaceAll(regexDateAtStart, "").trim();


            // Rimuove tutto tranne numeri, virgole, punti e segni meno
            String valueString = lineTextWithoutDate1.replaceAll("[^\\d.,-]", "").replace("€", "");

            // Sostituisce le virgole con punti per la conversione
            valueString = valueString.replace(".", "");
            valueString = valueString.replace(",", ".");


            return Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            // Gestisce il caso in cui la stringa non possa essere convertita in numero
            System.err.println("Error parsing value: " + lineText);
            return null;
        }
    }

}
