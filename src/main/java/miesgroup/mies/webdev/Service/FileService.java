package miesgroup.mies.webdev.Service;//package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Bolletta;
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
import java.sql.Date;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class FileService {

    private final FileRepo fileRepo;
    private final BollettaRepo bollettaRepo;
    private final BollettaService bollettaService;

    public FileService(FileRepo fileRepo, BollettaRepo bollettaRepo, BollettaService bollettaService) {
        this.fileRepo = fileRepo;
        this.bollettaRepo = bollettaRepo;
        this.bollettaService = bollettaService;
    }

    public int saveFile(String fileName, byte[] fileData) throws SQLException {
        if (fileName == null || fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("File name and data must not be null or empty");
        }
        PDFFile pdfFile = new PDFFile();
        pdfFile.setFile_Name(fileName);
        pdfFile.setFile_Data(fileData);
        return fileRepo.insert(pdfFile);
    }

    public PDFFile getFile(int id) {
        return fileRepo.findById(id);
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
    public void extractValuesFromXml(byte[] xmlData, String idPod) throws SQLException {
        ArrayList<Double> extractedValues = new ArrayList<>();
        String nomeBolletta = "";
        Date periodoInizio = null;
        Date periodoFine = null;

        int fasciaOrariaCount = 0; // Contatore per "Fascia oraria"
        int spesaMateriaEnergiaCount = 0; // Contatore per "SPESA PER LA MATERIA ENERGIA"
        int spesaTrasportoGestioneContatoreCount = 0; // Contatore per "SPESA PER IL TRASPORTO E LA GESTIONE DEL CONTATORE"
        int spesaOneriSistemaCount = 0; // Contatore per "SPESA PER ONERI DI SISTEMA"
        int totaleImposteCount = 0; // Contatore per "TOTALE IMPOSTE"
        int NomeBolletta = 0; // Contatore per "Nome Bolletta"
        int estrazioneDate = 0; // Contatore per "Data"

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

                    if (lineText.contains("Bolletta") && NomeBolletta < 1) {
                        NomeBolletta++;
                        nomeBolletta = extractBollettaNumero(lineText);
                    }
                    if (lineText.contains("Fascia oraria") && fasciaOrariaCount < 9) {
                        fasciaOrariaCount++;
                        ArrayList<Date> dates = extractDates(lineText);
                        if (dates.size() == 2 && estrazioneDate < 1) {
                            estrazioneDate++;
                            periodoInizio = dates.get(0);
                            periodoFine = dates.get(1);
                        }
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
            insertBolletta(extractedValues, nomeBolletta, periodoInizio, periodoFine, idPod);
            convertiDatainMese(periodoFine,nomeBolletta);
            bollettaService.A2AVerifica(nomeBolletta, idPod, extractedValues.get(9));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertBolletta(ArrayList<Double> extractedValues, String nomeBolletta, Date periodoInizio, Date periodoFine, String idPod) throws SQLException {
        Bolletta bolletta = new Bolletta();
        bolletta.setNomeBolletta(nomeBolletta);
        bolletta.setPeriodoInizio(periodoInizio);
        bolletta.setPeriodoFine(periodoFine);
        bolletta.setF1A(extractedValues.get(0));
        bolletta.setF2A(extractedValues.get(1));
        bolletta.setF3A(extractedValues.get(2));
        bolletta.setF1R(extractedValues.get(3));
        bolletta.setF2R(extractedValues.get(4));
        bolletta.setF3R(extractedValues.get(5));
        bolletta.setF1P(extractedValues.get(6));
        bolletta.setF2P(extractedValues.get(7));
        bolletta.setF3P(extractedValues.get(8));
        bolletta.setSpese_Energia(extractedValues.get(9));
        bolletta.setTrasporti(extractedValues.get(10));
        bolletta.setOneri(extractedValues.get(11));
        bolletta.setImposte(extractedValues.get(12));
        bolletta.setId_pod(idPod);
        bollettaRepo.A2Ainsert(bolletta);
    }

    public static String extractBollettaNumero(String lineText) {
        Pattern pattern = Pattern.compile("Bolletta n\\. (\\d+)");
        Matcher matcher = pattern.matcher(lineText);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    public static ArrayList<Date> extractDates(String lineText) {
        ArrayList<Date> dates = new ArrayList<>();
        Pattern datePattern = Pattern.compile("\\d{2}\\.\\d{2}\\.\\d{4}");
        Matcher matcher = datePattern.matcher(lineText);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        while (matcher.find()) {
            String dateString = matcher.group();
            try {
                Date date = new Date(dateFormat.parse(dateString).getTime());
                dates.add(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return dates;
    }

    private static Double extractValueFromLine(String lineText) {
        try {
            int contatoreF = 0;
            // Aggiungi log per il debug
            System.out.println("Extracting value from line: " + lineText);

            // Rimuovi i valori in formato data
            String regexDateAtStart = "^(\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\s+){1,2}";
            String lineTextWithoutDate1 = lineText.replaceAll(regexDateAtStart, "");

            String valueString;
            if (contatoreF < 9) {
                contatoreF++;
                // Rimuove la prima cifra numerica dopo la lettera "F"
                String remuveAfterF = lineTextWithoutDate1.replaceFirst("F\\d", "F");

                // Rimuove tutto tranne numeri, virgole, punti e segni meno
                valueString = remuveAfterF.replaceAll("[^\\d.,-]", "").replace("€", "");
            } else {
                // Rimuove tutto tranne numeri, virgole, punti e segni meno
                valueString = lineTextWithoutDate1.replaceAll("[^\\d.,-]", "").replace("€", "");
            }


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

    public void abbinaPod(int idFile, String idPod) {
        fileRepo.abbinaPod(idFile, idPod);
    }

    public void convertiDatainMese(Date dataFine, String nomeBolletta) throws SQLException {
        // Converte java.util.Date in java.time.LocalDate
        LocalDate localDate = dataFine.toLocalDate();

        // Definisci il formato del mese in italiano
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", Locale.ITALIAN);

        // Estrai il nome del mese
        String nomeMese = localDate.format(formatter);
        bollettaRepo.updateMeseBolletta(nomeMese, nomeBolletta);
    }
}
