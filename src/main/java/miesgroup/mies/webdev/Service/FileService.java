package miesgroup.mies.webdev.Service;//package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Model.BollettaPod;
import miesgroup.mies.webdev.Model.PDFFile;
import miesgroup.mies.webdev.Model.Periodo;
import miesgroup.mies.webdev.Model.Pod;
import miesgroup.mies.webdev.Repository.BollettaRepo;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Repository.FileRepo;
import miesgroup.mies.webdev.Repository.PodRepo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
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
    private final PodRepo podRepo;
    private final ClienteRepo clienteRepo;

    public FileService(FileRepo fileRepo, BollettaRepo bollettaRepo, BollettaService bollettaService, SessionService sessionService, PodRepo podRepo, ClienteRepo clienteRepo) {
        this.fileRepo = fileRepo;
        this.bollettaRepo = bollettaRepo;
        this.bollettaService = bollettaService;
        this.sessionService = sessionService;
        this.podRepo = podRepo;
        this.clienteRepo = clienteRepo;
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
            Map<String, Map<String, Double>> kWhPerMese = extractKwhPerMese(document);
            Periodo periodo = extractPeriodo(document);

            if (lettureMese.isEmpty()) {
                return null;
            }

            fileRepo.saveDataToDatabase(lettureMese, spesePerMese, idPod, nomeBolletta, piccoEFuoriPiccoPerMese, periodo, kWhPerMese);

            return nomeBolletta;

        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Transactional
    public void verificaA2APiuMesi(String nomeB) throws SQLException {
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

        // Set per tracciare le macro-categorie già inizializzate (per evitare reinizializzazioni)
        Set<String> categorieGiaViste = new HashSet<>();
        String categoriaCorrente = "";
        String sottoCategoria = "";
        // Flag per indicare che siamo nella sezione "Materia Energia"
        boolean sezioneCorretta = false;
        String meseCorrente = null;
        boolean controlloAttivo = false;
        int righeSenzaEuro = 0;

        // Parole chiave per interrompere il parsing (fine sezione)
        Set<String> stopParsingKeywords = Set.of(
                "TOTALE FORNITURA ENERGIA ELETTRICA E IMPOSTE",
                "RICALCOLO"
        );

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent().trim();

                // 0) Se la riga contiene "picco" o "Corrispettivi di dispacciamento Del.",
                // resetta sottoCategoria e le flag per permettere il ripristino delle nuove categorie
                if (lineText.contains("picco")) {
                    sottoCategoria = "";
                    categoriaCorrente = "";
                    sezioneCorretta = false;
                    continue; // Procede con la riga successiva
                }

                // 1) Interrompe il parsing se trova una parola chiave di stop
                if (stopParsingKeywords.stream().anyMatch(lineText::contains)) {
                    break;
                }

                // 2) Controlla se la riga contiene informazioni sul mese
                String meseEstratto = estraiMese(lineText);
                if (meseEstratto != null) {
                    meseCorrente = meseEstratto;
                }

                // 3) Identifica le macro-categorie principali
                if (lineText.contains("SPESA PER LA MATERIA ENERGIA")) {
                    categoriaCorrente = "Materia Energia";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    sezioneCorretta = false; // reset per la sotto-categoria
                    continue;
                }
                if (lineText.contains("SPESA PER ONERI DI SISTEMA")) {
                    categoriaCorrente = "Oneri di Sistema";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    // Non serve sottoCategoria per queste categorie
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

                // 3.1) Se siamo nella sezione "Materia Energia", distinguiamo le sottocategorie
                if ("Materia Energia".equals(categoriaCorrente) || sezioneCorretta) {
                    sezioneCorretta = true; // attiva la modalità di parsing della sezione
                    String lowerLine = lineText.toLowerCase();
                    // Prima controlla le perdite (case insensitive)
                    if (lowerLine.contains("perdite di rete f1")) {
                        sottoCategoria = "Perdite F1";
                    } else if (lowerLine.contains("perdite di rete f2")) {
                        sottoCategoria = "Perdite F2";
                    } else if (lowerLine.contains("perdite di rete f3")) {
                        sottoCategoria = "Perdite F3";
                    }
                    // Poi controlla le fasce specifiche
                    else if (lowerLine.contains("materia energia f1")) {
                        sottoCategoria = "Materia energia F1";
                    } else if (lowerLine.contains("materia energia f2")) {
                        sottoCategoria = "Materia energia F2";
                    } else if (lowerLine.contains("materia energia f3")) {
                        sottoCategoria = "Materia energia F3";
                    } else if (lowerLine.contains("materia energia")) {
                        sottoCategoria = "Materia energia F0";
                    } else if (lowerLine.contains("corrispettivi di dispacciamento del")) {
                        sottoCategoria = "dispacciamento";
                    }
                }

                //TODO:implementare auto somma delle penalità per impostare altro

                // 4) Se la riga contiene "€" ed è associata a una categoria, estrai il valore
                if ((!sottoCategoria.isEmpty() || !categoriaCorrente.isEmpty()) && lineText.contains("€")) {
                    Double valore = extractEuroValue(lineText);
                    if (valore != null) {
                        // Utilizza sottoCategoria se è presente, altrimenti usa categoriaCorrente
                        String chiave = !sottoCategoria.isEmpty() ? sottoCategoria : categoriaCorrente;
                        // Inizializza il controllo se è il primo valore per la macro-categoria
                        if (categorieGiaViste.contains(categoriaCorrente)) {
                            categorieGiaViste.remove(categoriaCorrente);
                            controlloAttivo = true;
                            righeSenzaEuro = 0;
                        }
                        if (meseCorrente == null) {
                            meseCorrente = "MeseSconosciuto";
                        }
                        spesePerMese
                                .computeIfAbsent(meseCorrente, k -> new HashMap<>())
                                .computeIfAbsent(chiave, k -> new ArrayList<>())
                                .add(valore);
                        controlloAttivo = true;
                        righeSenzaEuro = 0;
                        // Dopo l'inserimento, puoi decidere di resettare sottoCategoria se non ti serve per le righe successive:
                        // sottoCategoria = "";
                    }
                } else if (controlloAttivo) {
                    righeSenzaEuro++;
                    if (righeSenzaEuro >= 10 &&
                            !lineText.matches(".*(QUOTA|Componente|Corrispettivi|€/kWh|€/kW/mese|€/cliente/mese|QUOTA VARIABILE).*")) {
                        categoriaCorrente = "";
                        controlloAttivo = false;
                        righeSenzaEuro = 0;
                        sottoCategoria = "";
                        sezioneCorretta = false;
                    }
                } else {
                    righeSenzaEuro = 0;
                }
            }
        }
        System.out.println("Estrazione costi (spese): " + spesePerMese);
        return processSpesePerMese(spesePerMese);
    }


    private Map<String, Map<String, Double>> extractKwhPerMese(Document document) {
        // Struttura dati: mese -> (sotto-categoria -> lista di valori kWh)
        Map<String, Map<String, List<Double>>> kwhPerMese = new HashMap<>();

        // Set per tracciare le macro-categorie già inizializzate (usato per inizializzare la sezione una sola volta)
        Set<String> categorieGiaViste = new HashSet<>();
        // Variabili per la gestione delle categorie e sottocategorie
        String categoriaCorrente = "";
        String sottoCategoria = "";
        boolean sezioneCorretta = false;
        String meseCorrente = null;
        boolean controlloAttivo = false;
        int righeSenzaKwh = 0;

        // Parole chiave per interrompere il parsing (fine sezione)
        Set<String> stopParsingKeywords = Set.of(
                "Corrispettivi di dispacciamento Del.",
                "picco"
        );

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent().trim();

                // 1) Se la riga contiene una parola chiave di stop, interrompe il parsing
                if (stopParsingKeywords.stream().anyMatch(lineText::contains)) {
                    break;
                }

                // 2) Controlla se la riga contiene informazioni sul mese
                String meseEstratto = estraiMese(lineText);
                if (meseEstratto != null) {
                    meseCorrente = meseEstratto;
                }

                // 3) Identifica la macro-categoria "SPESA PER LA MATERIA ENERGIA"
                if (lineText.contains("SPESA PER LA MATERIA ENERGIA")) {
                    categoriaCorrente = "Materia Energia";
                    categorieGiaViste.add(categoriaCorrente);
                    controlloAttivo = false;
                    righeSenzaKwh = 0;
                    sezioneCorretta = false; // reset della flag per la sotto-categoria
                    continue;
                }

                // 3.1) Se siamo nella sezione "Materia Energia", distinguiamo le sottocategorie
                // Attiviamo la modalità "sezioneCorretta" per le righe successive
                if ("Materia Energia".equals(categoriaCorrente) || sezioneCorretta) {
                    sezioneCorretta = true;
                    String lowerLine = lineText.toLowerCase();
                    // Controlla prima le perdite, poi le fasce specifiche
                    if (lowerLine.contains("perdite di rete f1")) {
                        sottoCategoria = "Perdite F1";
                    } else if (lowerLine.contains("perdite di rete f2")) {
                        sottoCategoria = "Perdite F2";
                    } else if (lowerLine.contains("perdite di rete f3")) {
                        sottoCategoria = "Perdite F3";
                    } else if (lowerLine.contains("materia energia f1")) {
                        sottoCategoria = "Materia energia F1";
                    } else if (lowerLine.contains("materia energia f2")) {
                        sottoCategoria = "Materia energia F2";
                    } else if (lowerLine.contains("materia energia f3")) {
                        sottoCategoria = "Materia energia F3";
                    } else if (lowerLine.contains("materia energia")) {
                        sottoCategoria = "Materia energia F0";
                    }
                }

                // 4) Se la riga contiene "kWh" ed è stata determinata una sotto-categoria, estrai il valore
                if (!sottoCategoria.isEmpty() && lineText.contains("kWh")) {
                    Double kwhValue = extractKwhValue(lineText);
                    if (kwhValue != null) {
                        // Se è il primo valore per la macro-categoria, rimuoviamo la voce da categorieGiaViste per evitare inizializzazioni ripetute
                        if (categorieGiaViste.contains(categoriaCorrente)) {
                            categorieGiaViste.remove(categoriaCorrente);
                            controlloAttivo = true;
                            righeSenzaKwh = 0;
                        }
                        // Se il mese non è stato ancora impostato, usa un default
                        if (meseCorrente == null) {
                            meseCorrente = "MeseSconosciuto";
                        }
                        // Salva il valore kWh nella mappa usando la sotto-categoria come chiave
                        kwhPerMese
                                .computeIfAbsent(meseCorrente, k -> new HashMap<>())
                                .computeIfAbsent(sottoCategoria, k -> new ArrayList<>())
                                .add(kwhValue);
                        controlloAttivo = true;
                        righeSenzaKwh = 0;
                    }
                } else if (controlloAttivo) {
                    // Se la riga non contiene "kWh" ma siamo in modalità controllo, incrementa il contatore
                    righeSenzaKwh++;
                    if (righeSenzaKwh >= 10 &&
                            !lineText.matches(".*(QUOTA|Componente|Corrispettivi|€/kWh|€/kW/mese|€/cliente/mese|QUOTA VARIABILE).*")) {
                        categoriaCorrente = "";
                        controlloAttivo = false;
                        righeSenzaKwh = 0;
                        sottoCategoria = "";
                        sezioneCorretta = false;
                    }
                } else {
                    righeSenzaKwh = 0;
                }
            }
        }
        System.out.println("Estrazione kwh: " + kwhPerMese);
        return processKwhPerMese(kwhPerMese);
    }

    // Metodo di supporto per sommare i valori kWh per mese e categoria
    private Map<String, Map<String, Double>> processKwhPerMese(Map<String, Map<String, List<Double>>> kwhPerMese) {
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, List<Double>>> entryMese : kwhPerMese.entrySet()) {
            String mese = entryMese.getKey();
            Map<String, Double> catToSum = new HashMap<>();
            for (Map.Entry<String, List<Double>> entryCat : entryMese.getValue().entrySet()) {
                String categoria = entryCat.getKey();
                double somma = entryCat.getValue().stream().mapToDouble(Double::doubleValue).sum();
                catToSum.put(categoria, somma);
            }
            result.put(mese, catToSum);
        }
        return result;
    }

    private Double extractKwhValue(String text) {
        // La regex seguente cerca numeri nel formato tipico italiano, es:
        // "229.226,00 kWh" oppure "484,00 kWh"
        Pattern pattern = Pattern.compile("([0-9]{1,3}(?:\\.[0-9]{3})*(?:,[0-9]{1,2})?)\\s*kWh");
        Matcher matcher = pattern.matcher(text);
        Double lastMatch = null;
        while (matcher.find()) {
            String match = matcher.group(1);
            // Rimuove i separatori delle migliaia (punti) e sostituisce la virgola con un punto
            match = match.replace(".", "").replace(",", ".");
            try {
                lastMatch = Double.parseDouble(match);
            } catch (NumberFormatException e) {
                System.err.println("Errore nel parsing del valore kWh: " + match);
            }
        }
        if (lastMatch != null) {
            System.out.println("✅ Valore kWh estratto: " + lastMatch);
            return lastMatch;
        } else {
            System.out.println("❌ Nessun valore kWh trovato in: " + text);
            return null;
        }
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

    public List<BollettaPod> getDati(int idSessione) {
        List<Pod> p = podRepo.find("utente", clienteRepo.findById(sessionService.trovaUtentebBySessione(idSessione))).list();
        List<BollettaPod> bollette = new ArrayList<>();
        for (Pod pod : p) {
            bollette.addAll(bollettaRepo.find("idPod", pod.getId()).list());
        }
        return bollette;
    }

    public List<BollettaPod> getDatiRicalcoli(int idSessione, String idPod) {
        return bollettaRepo.find("idPod", idPod).list();
    }

    @Transactional
    public void verificaA2APostRicalcoli(BollettaPod bolletta) {
        bollettaService.A2AVerifica(bolletta);
    }


    @Transactional
    public void controlloRicalcoliInBolletta(byte[] xmlData, String idPod, String nomeB, Integer idSessione) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlData));

            Map<String, Map<String, Double>> ricalcoliPerMese = extractRicalcoliPerMese(document);
            Periodo periodo = extractPeriodo(document);

            if (ricalcoliPerMese.isEmpty()) {
                return;
            }

            // Recupera l'anno di riferimento dal periodo
            String annoRicalcolo = periodo.getAnno();  // Es. "2023"

            // Recupera le bollette esistenti per la sessione attuale
            List<BollettaPod> bolletteEsistenti = getDatiRicalcoli(idSessione, idPod);

            // Itera sui ricalcoli trovati
            for (Map.Entry<String, Map<String, Double>> entry : ricalcoliPerMese.entrySet()) {
                String meseRicalcolo = entry.getKey();  // Es: "giugno"
                Map<String, Double> valoriRicalcolati = entry.getValue();

                // Trova una bolletta esistente per lo stesso mese e anno
                Optional<BollettaPod> bollettaEsistenteOpt = bolletteEsistenti.stream()
                        .filter(b -> b.getMese().equalsIgnoreCase(meseRicalcolo) &&
                                b.getAnno().equals(annoRicalcolo) &&
                                b.getIdPod().equals(idPod))
                        .findFirst();

                if (bollettaEsistenteOpt.isPresent()) {
                    // Se la bolletta esiste, aggiorniamo i suoi valori
                    BollettaPod bollettaEsistente = bollettaEsistenteOpt.get();
                    aggiornaBollettaConRicalcoli(bollettaEsistente, valoriRicalcolati);
                    bollettaRepo.updateBolletta(bollettaEsistente);
                    verificaA2APostRicalcoli(bollettaEsistente);
                } else {
                    // Se la bolletta non esiste, salviamo i ricalcoli come nuova voce
                    bollettaRepo.saveRicalcoliToDatabase(ricalcoliPerMese, idPod, nomeB, periodo);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }


    private void aggiornaBollettaConRicalcoli(BollettaPod bolletta, Map<String, Double> valoriRicalcolati) {
        if (valoriRicalcolati.containsKey("Ricalcolo Materia Energia")) {
            bolletta.setSpeseEnergia(valoriRicalcolati.get("Ricalcolo Materia Energia"));
        }
        if (valoriRicalcolati.containsKey("Ricalcolo Trasporto e Gestione Contatore")) {
            bolletta.setTrasporti(valoriRicalcolati.get("Ricalcolo Trasporto e Gestione Contatore"));
        }
        if (valoriRicalcolati.containsKey("Ricalcolo Oneri di Sistema")) {
            bolletta.setOneri(valoriRicalcolati.get("Ricalcolo Oneri di Sistema"));
        }
        if (valoriRicalcolati.containsKey("Ricalcolo Imposte")) {
            bolletta.setImposte(valoriRicalcolati.get("Ricalcolo Imposte"));
        }
    }


    private Map<String, Map<String, Double>> extractRicalcoliPerMese(Document document) {
        // Struttura dati intermedia: mese -> (categoria -> lista di valori)
        Map<String, Map<String, List<Double>>> ricalcoliPerMese = new HashMap<>();

        String categoriaCorrente = null;
        String meseCorrente = null; // Campo per il mese/periodo in parsing
        boolean controlloAttivo = false;
        int righeSenzaEuro = 0;

        // Parola chiave per interrompere definitivamente il parsing
        String stopParsingKeyword = "TOTALE FORNITURA ENERGIA ELETTRICA E IMPOSTE";

        NodeList lineNodes = document.getElementsByTagName("Line");
        for (int i = 0; i < lineNodes.getLength(); i++) {
            Node lineNode = lineNodes.item(i);
            if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                String lineText = lineNode.getTextContent().trim();

                // 1) Interrompe il parsing definitivamente se trova la parola chiave
                if (lineText.contains(stopParsingKeyword)) {
                    break;
                }

                // 2) Controlla se la riga contiene informazioni sul mese
                String meseEstratto = estraiMese(lineText);
                if (meseEstratto != null) {
                    meseCorrente = meseEstratto;
                }

                // 3) Identificare la categoria corrente (RICALCOLI)
                if (lineText.contains("RICALCOLO PER RETTIFICA SPESA PER LA MATERIA ENERGIA")) {
                    categoriaCorrente = "Ricalcolo Materia Energia";
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("RICALCOLO PER RETTIFICA SPESA PER IL TRASPORTO E LA GESTIONE")) {
                    categoriaCorrente = "Ricalcolo Trasporto e Gestione Contatore";
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("RICALCOLO PER RETTIFICA SPESA PER ONERI DI SISTEMA")) {
                    categoriaCorrente = "Ricalcolo Oneri di Sistema";
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }
                if (lineText.contains("RICALCOLO PER RETTIFICA IMPOSTE")) {
                    categoriaCorrente = "Ricalcolo Imposte";
                    controlloAttivo = false;
                    righeSenzaEuro = 0;
                    continue;
                }

                // 4) Se la categoria è attiva e troviamo un valore monetario (€), lo estraiamo
                if (categoriaCorrente != null && lineText.contains("€")) {
                    Double valore = extractEuroValue(lineText);
                    if (valore != null) {
                        // Se non è stato ancora impostato un mese, assegniamo un valore di default
                        if (meseCorrente == null) {
                            meseCorrente = "MeseSconosciuto";
                        }

                        // Aggiunge il valore nella struttura (mese -> categoria -> valori)
                        ricalcoliPerMese
                                .computeIfAbsent(meseCorrente, k -> new HashMap<>())
                                .computeIfAbsent(categoriaCorrente, k -> new ArrayList<>())
                                .add(valore);

                        controlloAttivo = true;
                        righeSenzaEuro = 0;
                    }
                } else if (controlloAttivo) {
                    // Se abbiamo attivato il controllo e la riga non ha €, incrementiamo il contatore
                    righeSenzaEuro++;

                    // Se sono passate troppe righe senza €, resettiamo la categoria
                    if (righeSenzaEuro >= 10 &&
                            !lineText.matches(".*(QUOTA|Componente|Corrispettivi|€/kWh|€/kW/mese|€/cliente/mese|QUOTA VARIABILE).*")) {

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
        return processSpesePerMese(ricalcoliPerMese);
    }
}