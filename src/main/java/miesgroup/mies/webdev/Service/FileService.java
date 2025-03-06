package miesgroup.mies.webdev.Service;//package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Persistance.Model.BollettaPod;
import miesgroup.mies.webdev.Persistance.Model.PDFFile;
import miesgroup.mies.webdev.Persistance.Model.Periodo;
import miesgroup.mies.webdev.Persistance.Repository.BollettaRepo;
import miesgroup.mies.webdev.Persistance.Repository.FileRepo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
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
import org.xml.sax.SAXException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class FileService {

    private final FileRepo fileRepo;
    private final BollettaRepo bollettaRepo;
    private final BollettaService bollettaService;
    private final SessionService sessionService;

    public FileService(FileRepo fileRepo, BollettaRepo bollettaRepo, BollettaService bollettaService, SessionService sessionService) {
        this.fileRepo = fileRepo;
        this.bollettaRepo = bollettaRepo;
        this.bollettaService = bollettaService;
        this.sessionService = sessionService;
    }

    @Transactional
    public int saveFile(String fileName, byte[] fileData) throws SQLException {
        if (fileName == null || fileData == null || fileData.length == 0) {
            throw new IllegalArgumentException("File name and data must not be null or empty");
        }
        PDFFile pdfFile = new PDFFile();
        pdfFile.setFileName(fileName);
        pdfFile.setFileData(fileData);
        return fileRepo.insert(pdfFile);
    }

    @Transactional
    public PDFFile getFile(int id) {
        return fileRepo.findById(id);
    }


    //CONVERTI FILE IN XML
    @Transactional
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

    @Transactional
    public String convertDocumentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "2");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }


    @Transactional
    public String extractValuesFromXmlA2A(byte[] xmlData, String idPod) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlData));

            String nomeBolletta = extractBollettaNome(document);
            if (nomeBolletta == null) {
                return null;
            } else {
                bollettaService.A2AisPresent(nomeBolletta, idPod);
            }

            Map<String, Map<String, Map<String, Integer>>> lettureMese = extractLetture(document);
            Map<String, Map<String, Map<String, Double>>> piccoEFuoriPiccoPerMese = extractPiccoFuoriPicco(document);
            Map<String, Map<String, Double>> spesePerMese = extractSpesePerMese(document);
            Periodo periodo = extractPeriodo(document);

            if (lettureMese.isEmpty()) {
                System.err.println("Nessuna lettura valida trovata.");
                return null;
            }

            fileRepo.saveDataToDatabase(lettureMese, spesePerMese, idPod, nomeBolletta, piccoEFuoriPiccoPerMese, periodo);

            return nomeBolletta;

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Transactional
    public void verificaA2A(String nomeB) throws SQLException {
        List<BollettaPod> b = bollettaRepo.find("nomeBolletta", nomeB).list();
        for (BollettaPod bollettaPod : b) {
            bollettaService.A2AVerifica(bollettaPod);
        }

    }

    private static Map<String, Map<String, Map<String, Double>>> extractPiccoFuoriPicco(Document document) {
        Map<String, Map<String, Map<String, Double>>> piccoEFuoriPicco = new HashMap<>();
        String categoriaCorrente = null;
        boolean attesaPicco = false; // Flag per capire se stiamo aspettando "picco" dopo "fuori"

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent().trim();

                // Caso in cui troviamo "Corrispettivo mercato capacità ore picco"
                if (lineText.contains("Corrispettivo mercato capacità ore picco")) {
                    categoriaCorrente = "Picco";
                    attesaPicco = false;
                    continue;
                }

                // Caso in cui troviamo "Corrispettivo mercato capacità ore fuori"
                if (lineText.contains("Corrispettivo mercato capacità ore fuori")) {
                    categoriaCorrente = null;
                    attesaPicco = true; // Flag per attendere la parola "picco" nella riga successiva
                    continue;
                }

                // Se la riga successiva contiene solo "picco", completa la categoria "Fuori Picco"
                if (attesaPicco && lineText.equalsIgnoreCase("picco")) {
                    categoriaCorrente = "Fuori Picco";
                    attesaPicco = false;
                    continue;
                }

                // Se troviamo una riga con "€/kWh" ed è associata a una categoria
                if (categoriaCorrente != null && lineText.contains("€/kWh")) {
                    ArrayList<Date> dates = extractDates(lineText);
                    Double valoreKWh = extractKWhFromLine(lineText);
                    Double costoEuro = extractEuroValue(lineText);

                    if (dates.size() == 2 && valoreKWh != null && costoEuro != null) {
                        String mese = DateUtils.getMonthFromDateLocalized(dates.get(1));

                        // Creiamo la struttura della mappa se non esiste
                        piccoEFuoriPicco.putIfAbsent(mese, new HashMap<>());
                        Map<String, Map<String, Double>> categorie = piccoEFuoriPicco.get(mese);
                        categorie.putIfAbsent(categoriaCorrente, new HashMap<>());

                        // Salviamo i valori di consumo e costo
                        Map<String, Double> valori = categorie.get(categoriaCorrente);
                        valori.put("kWh", valoreKWh);
                        valori.put("€", costoEuro);
                    }
                }

                // Se troviamo una riga senza "€/kWh" e non è una nuova categoria, resettiamo la categoria corrente
                if (!lineText.contains("€/kWh") && !lineText.toLowerCase().contains("corrispettivo mercato capacità")) {
                    categoriaCorrente = null;
                }
            }
        }
        return piccoEFuoriPicco;
    }


    private Periodo extractPeriodo(Document document) {
        NodeList lineNodes = document.getElementsByTagName("Line");

        Date dataInizio = null;
        Date dataFine = null;

        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent();

                if (lineText.contains("Fascia oraria")) {
                    // Cerca date in formato DD.MM.YYYY
                    ArrayList<Date> dates = extractDates(lineText);

                    if (dates.size() == 2) {
                        dataInizio = dates.get(0);
                        dataFine = dates.get(1);
                        break; // Troviamo la prima riga valida con entrambe le date
                    }
                }
            }
        }

        if (dataInizio != null && dataFine != null) {
            // Estrai l'anno dalla data di fine
            String anno = String.valueOf(dataFine.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());
            return new Periodo(dataInizio, dataFine, anno);
        } else {
            throw new IllegalStateException("Impossibile estrarre il periodo: date mancanti.");
        }
    }


    private Map<String, Map<String, Double>> extractSpesePerMese(Document document) {
        // Struttura dati intermedia: mese -> (categoria -> lista di valori)
        Map<String, Map<String, List<Double>>> spesePerMese = new HashMap<>();

        Set<String> categorieGiaViste = new HashSet<>();
        String categoriaCorrente = null;
        String meseCorrente = null; // Campo per il mese/periodo in parsing
        boolean controlloAttivo = false;
        int righeSenzaEuro = 0;

        // Parole chiave per interrompere il parsing
        Set<String> stopParsingKeywords = Set.of(
                "TOTALE FORNITURA ENERGIA ELETTRICA E IMPOSTE",
                "RICALCOLO"
        );

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent().trim();

                // 1) Interrompe il parsing se trova un titolo che indica fine sezione
                if (stopParsingKeywords.stream().anyMatch(lineText::contains)) {
                    System.out.println("🚨 Interruzione del parsing: trovata riga '" + lineText + "'");
                    break;
                }

                // 2) Controlla se la riga contiene informazioni sul mese
                //    (NB: qui dovrai adattare la logica di estrazione al formato delle tue bollette)
                String meseEstratto = estraiMese(lineText);
                if (meseEstratto != null) {
                    meseCorrente = meseEstratto;
                    System.out.println("📅 Mese corrente: " + meseCorrente);
                }

                // 3) Identificare la categoria corrente
                if (lineText.contains("SPESA PER LA MATERIA ENERGIA")) {
                    categoriaCorrente = "Materia Energia";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("SPESA PER ONERI DI SISTEMA")) {
                    categoriaCorrente = "Oneri di Sistema";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("SPESA PER IL TRASPORTO E LA GESTIONE DEL CONTATORE")) {
                    categoriaCorrente = "Trasporto e Gestione Contatore";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("TOTALE IMPOSTE")) {
                    categoriaCorrente = "Totale Imposte";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }

                // 4) Se la categoria è attiva e troviamo un valore monetario (€), lo estraiamo
                if (categoriaCorrente != null && lineText.contains("€")) {
                    Double valore = extractEuroValue(lineText);
                    if (valore != null) {
                        // Assicura che il primo valore venga sommato correttamente
                        if (categorieGiaViste.contains(categoriaCorrente)) {
                            categorieGiaViste.remove(categoriaCorrente);
                            controlloAttivo = true;
                            righeSenzaEuro = 0;
                        }

                        // Se non è stato ancora impostato un mese, assegniamo un mese di default
                        if (meseCorrente == null) {
                            meseCorrente = "MeseSconosciuto";
                        }

                        // Aggiunge il valore nella struttura (mese -> categoria -> valori)
                        spesePerMese
                                .computeIfAbsent(meseCorrente, k -> new HashMap<>())
                                .computeIfAbsent(categoriaCorrente, k -> new ArrayList<>())
                                .add(valore);

                        controlloAttivo = true;
                        righeSenzaEuro = 0;
                    }
                } else if (controlloAttivo) {
                    // Se abbiamo attivato il controllo e la riga non ha €, incrementiamo il contatore
                    righeSenzaEuro++;

                    // Se sono passate 10 righe senza €, resettiamo la categoria (no parse di nuove spese)
                    if (righeSenzaEuro >= 10 &&
                            !lineText.matches(".*(QUOTA|Componente|Corrispettivi|€/kWh|€/kW/mese|€/cliente/mese|QUOTA VARIABILE).*")) {

                        System.out.println("🔄 Reset categoria corrente dopo 10 righe senza €");
                        categoriaCorrente = null;
                        controlloAttivo = false;
                        righeSenzaEuro = 0;
                    }
                } else {
                    // Se troviamo un'altra riga con €, resettiamo il contatore per evitare reset prematuri
                    righeSenzaEuro = 0;
                }
            }
        }

        // Una volta terminato il parsing, andiamo a processare e sommare i dati
        return processSpesePerMese(spesePerMese);
    }

    /**
     * Metodo che, data la struttura dati con chiave (mese, categoria), somma i valori
     * e produce una mappa (mese -> (categoria -> valore totale)).
     */
    private Map<String, Map<String, Double>> processSpesePerMese(
            Map<String, Map<String, List<Double>>> spesePerMese) {

        Map<String, Map<String, Double>> speseFinali = new HashMap<>();

        for (Map.Entry<String, Map<String, List<Double>>> entryMese : spesePerMese.entrySet()) {
            String mese = entryMese.getKey();
            Map<String, Double> categorieSomma = new HashMap<>();

            for (Map.Entry<String, List<Double>> entryCat : entryMese.getValue().entrySet()) {
                String categoria = entryCat.getKey();
                double somma = entryCat.getValue().stream().mapToDouble(Double::doubleValue).sum();
                categorieSomma.put(categoria, somma);
            }

            speseFinali.put(mese, categorieSomma);
        }

        System.out.println("📊 Spese finali: " + speseFinali);
        return speseFinali;
    }

    /**
     * Esempio di metodo per estrarre il mese da una riga del documento.
     * Da adattare a seconda del formato effettivo (ad es. "Periodo di riferimento: 01/03/2023 - 31/03/2023").
     */
    private String estraiMese(String lineText) {
        // Cerco il pattern "dd.MM.yyyy", ad es. "01.01.2024"
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
        Matcher matcher = pattern.matcher(lineText);

        if (matcher.find()) {
            // Prendo la prima data trovata: es. "01.01.2024"
            String dataTrovata = matcher.group(1);

            // Parso la stringa in LocalDate
            LocalDate parsedDate = LocalDate.parse(dataTrovata,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            // Ora costruisco la stringa che vuoi salvare.
            // Esempio: "gennaio 2024" in italiano
            String nomeMese = parsedDate.getMonth()
                    .getDisplayName(TextStyle.FULL, Locale.ITALIAN);
            //int anno = parsedDate.getYear();

            // Ritorno "gennaio 2024"
            return nomeMese;
        }

        // Se non trova nulla, ritorno null
        return null;
    }


    private Map<String, Map<String, Map<String, Integer>>> extractLetture(Document document) {
        Map<String, Map<String, Map<String, Integer>>> lettureMese = new HashMap<>();
        String categoriaCorrente = null;

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent();


                // Gestione delle categorie
                if (lineText.contains("ENERGIA ATTIVA")) {
                    categoriaCorrente = "Energia Attiva";
                    continue;
                }
                if (lineText.contains("ENERGIA REATTIVA")) {
                    categoriaCorrente = "Energia Reattiva";
                    continue;
                }
                if (lineText.contains("POTENZA")) {
                    categoriaCorrente = "Potenza";
                    continue;
                }

                // Estrarre i dati solo se siamo in una categoria valida
                if (lineText.contains("Fascia oraria") && categoriaCorrente != null) {
                    ArrayList<Date> dates = extractDates(lineText);
                    Double value = extractValueFromLine(lineText);
                    String fascia = extractFasciaOraria(lineText);

                    if (dates.size() == 2 && value != null && fascia != null) {
                        String mese = DateUtils.getMonthFromDateLocalized(dates.get(1));
                        // Ora puoi usare "mese" come necessario
                        System.out.println("Mese: " + mese);

                        lettureMese.putIfAbsent(mese, new HashMap<>());
                        Map<String, Map<String, Integer>> categorie = lettureMese.get(mese);
                        categorie.putIfAbsent(categoriaCorrente, new HashMap<>());

                        Map<String, Integer> letture = categorie.get(categoriaCorrente);
                        letture.put(fascia, letture.getOrDefault(fascia, 0) + value.intValue());
                    }
                }
            }
        }
        return lettureMese;
    }

    private static Double extractEuroValue(String lineText) {
        try {
            System.out.println("🧐 Tentativo di estrarre valore monetario da: " + lineText);

            // Regex migliorato per supportare più formati
            String regex = "€\\s*([0-9]+(?:\\.[0-9]{3})*,[0-9]+)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(lineText);

            if (matcher.find()) {
                String valueString = matcher.group(1);

                // Rimuove i separatori delle migliaia (i punti) MA mantiene il separatore decimale (virgola -> punto)
                valueString = valueString.replaceAll("\\.(?=[0-9]{3},)", "").replace(",", ".");

                System.out.println("✅ Valore estratto: " + valueString);
                return Double.parseDouble(valueString);
            } else {
                System.out.println("❌ Nessun valore in € trovato in: " + lineText);
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Errore durante il parsing del valore in euro: " + lineText);
        }
        return null; // Nessun valore trovato o errore nel parsing
    }


    private String extractBollettaNome(Document document) {
        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent();
                if (lineText.contains("Bolletta n")) {
                    return extractBollettaNumero(lineText);
                }
            }
        }
        return null;
    }


    private static String extractFasciaOraria(String lineText) {
        String regex = "F\\d"; // Cerca "F1", "F2", "F3"
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(lineText);

        if (matcher.find()) {
            return matcher.group(); // Restituisce "F1", "F2" o "F3"
        }
        return null; // Nessuna fascia trovata
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
            // Log per il debug
            System.out.println("Extracting value from line: " + lineText);

            // Rimuove i valori in formato data (se presenti all'inizio della riga)
            String regexDateAtStart = "^(\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\s+){1,2}";
            String lineTextWithoutDate = lineText.replaceAll(regexDateAtStart, "");

            // Rimuove la prima cifra numerica dopo la lettera "F", se presente
            String lineTextWithoutF = lineTextWithoutDate.replaceFirst("F\\d", "F");

            // Rimuove tutto tranne numeri, virgole, punti e segni meno
            String valueString = lineTextWithoutF.replaceAll("[^\\d.,-]", "").replace("€", "");

            // Sostituisce le virgole con punti per la conversione
            valueString = valueString.replace(".", "").replace(",", ".");

            // Converte il valore in Double
            return Double.parseDouble(valueString);
        } catch (NumberFormatException e) {
            // Gestisce il caso in cui la stringa non possa essere convertita in numero
            System.err.println("Error parsing value: " + lineText);
            return null;
        }
    }

    private static Double extractKWhFromLine(String lineText) {
        Pattern pattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d+)?)\\s*kWh", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(lineText);
        if (matcher.find()) {
            String value = matcher.group(1).replace(".", "").replace(",", "."); // Normalizza i separatori
            Double numero = Double.parseDouble(value);
            return numero;
        }
        return null;
    }


    @Transactional
    public void abbinaPod(int idFile, String idPod) {
        fileRepo.abbinaPod(idFile, idPod);
    }

    @Transactional
    public byte[] getXmlData(int id) {
        return fileRepo.getFile(id);
    }
}