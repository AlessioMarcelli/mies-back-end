package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import miesgroup.mies.webdev.Model.Cliente;
import miesgroup.mies.webdev.Model.PDFFile;
import miesgroup.mies.webdev.Model.Pod;
import miesgroup.mies.webdev.Repository.ClienteRepo;
import miesgroup.mies.webdev.Repository.FixingRepo;
import miesgroup.mies.webdev.Repository.PodRepo;
import miesgroup.mies.webdev.Repository.SessionRepo;
import miesgroup.mies.webdev.Rest.Exception.NotYourPodException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PodService {

    private final PodRepo podRepo;
    private final SessionService sessionService;
    private final SessionRepo sessionRepo;
    private final ClienteRepo clienteRepo;
    private final FixingRepo fixingRepo;

    public PodService(PodRepo podRepo, SessionService sessionService, SessionRepo sessionRepo, ClienteRepo clienteRepo, FixingRepo fixingRepo) {
        this.podRepo = podRepo;
        this.sessionService = sessionService;
        this.sessionRepo = sessionRepo;
        this.clienteRepo = clienteRepo;
        this.fixingRepo = fixingRepo;
    }


    @Transactional
    public String extractValuesFromXml(byte[] xmlData, int sessione) {
        ArrayList<Double> extractedValues = new ArrayList<>();
        String id_pod = "";
        int id_utente = sessionService.trovaUtentebBySessione(sessione);
        String fornitore = "";
        // Divide l'indirizzo in sede, CAP e città
        String sede = "";
        String cap = "";
        String citta = "";
        boolean esiste = false;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(xmlData);
            Document document = builder.parse(is);

            NodeList lineNodes = document.getElementsByTagName("Line");
            int estrai = 0;
            for (int i = 0; i < lineNodes.getLength() && !esiste; i++) {
                Node lineNode = lineNodes.item(i);
                if (lineNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element lineElement = (Element) lineNode;
                    String lineText = lineElement.getTextContent();

                    if (lineText.contains("POD") && estrai < 1) {
                        Node LineNode = lineNodes.item(i + 1);
                        Element LineElement = (Element) LineNode;
                        id_pod = LineElement.getTextContent();
                    }

                    if (podRepo.verificaSePodEsiste(id_pod, id_utente) == null) {
                        if (lineText.contains("SEGNALAZIONE GUASTI ELETTRICITA")) {
                            Node LineNode = lineNodes.item(i + 2);
                            Element LineElement = (Element) LineNode;
                            fornitore = LineElement.getTextContent();
                        }
                        if (lineText.contains("Tensione di alimentazione") || lineText.contains("Potenza impegnata") || lineText.contains("Potenza disponibile") && estrai < 3) {
                            estrai++;
                            if (i + 1 < lineNodes.getLength()) {
                                Node nextLineNode = lineNodes.item(i + 1);
                                if (nextLineNode.getNodeType() == Node.ELEMENT_NODE) {
                                    Element nextLineElement = (Element) nextLineNode;
                                    String nextLineText = nextLineElement.getTextContent();

                                    // Rimuovi i valori in formato data
                                    String regexDateAtStart = "^(\\d{1,2}\\.\\d{1,2}\\.\\d{2,4}\\s+){1,2}";
                                    String lineTextWithoutDate = nextLineText.replaceAll(regexDateAtStart, "").trim();

                                    // Rimuove tutto tranne numeri, virgole, punti e segni meno
                                    String valueString = lineTextWithoutDate.replaceAll("[^\\d.,-]", "").replace("€", "");

                                    // Sostituisce le virgole con punti per la conversione
                                    valueString = valueString.replace(".", "");
                                    valueString = valueString.replace(",", ".");

                                    try {
                                        Double value = Double.parseDouble(valueString);
                                        extractedValues.add(value);
                                    } catch (NumberFormatException e) {
                                        System.err.println("Error parsing value: " + nextLineText);
                                    }
                                }
                            }
                        }

                        if (lineText.contains("Indirizzo di fornitura")) {
                            Node LineNode = lineNodes.item(i + 1);
                            if (LineNode != null && LineNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element LineElement = (Element) LineNode;
                                String indirizzo = LineElement.getTextContent().trim(); // Ottiene l'indirizzo


                                String[] parts = indirizzo.split(" - ");
                                if (parts.length == 2) {
                                    sede = parts[0].trim();
                                    String capCitta = parts[1].trim();

                                    // Divide CAP e Città basandosi sul primo spazio
                                    String[] capCittaParts = capCitta.split(" ", 2);
                                    cap = capCittaParts[0];
                                    citta = capCittaParts.length > 1 ? capCittaParts[1] : "";
                                } else {
                                    System.out.println("❌ Errore: Formato dell'indirizzo non valido.");
                                }

                                // Stampa i risultati per verifica
                                System.out.println("📌 Sede: " + sede);
                                System.out.println("📌 CAP: " + cap);
                                System.out.println("📌 Città: " + citta);
                            }
                        }

                    } else {
                        esiste = true;
                        id_pod = podRepo.verificaSePodEsiste(id_pod, id_utente);
                        return id_pod;
                    }
                }
            }
            creaPod(extractedValues, id_utente, id_pod, fornitore, citta, cap, sede);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return id_pod;
    }


    @Transactional
    public void creaPod(ArrayList<Double> extractedValues, int id_utente, String id_pod, String fornitore, String citta, String cap, String sede) {
        Cliente c = clienteRepo.findById(id_utente);
        Pod pod = new Pod();
        pod.setUtente(c);
        pod.setId(id_pod);
        pod.setFornitore(fornitore);
        pod.setTensioneAlimentazione(extractedValues.get(0));
        pod.setPotenzaImpegnata(extractedValues.get(1));
        pod.setPotenzaDisponibile(extractedValues.get(2));
        pod.setSede(sede);
        pod.setNazione(citta);
        pod.setCap(cap);
        if (pod.getTensioneAlimentazione() <= 1000.0) {
            pod.setTipoTensione("Bassa");
        } else if (pod.getTensioneAlimentazione() > 1000.0 && pod.getTensioneAlimentazione() <= 35000.0) {
            pod.setTipoTensione("Media");
        } else {
            pod.setTipoTensione("Alta");
        }
        podRepo.persist(pod);
    }

    @Transactional
    public List<Pod> tutti(int id_sessione) {
        return podRepo.findAll(sessionRepo.find(id_sessione));
    }

    @Transactional
    public Pod getPod(String id, int id_utente) {
        return podRepo.cercaIdPod(id, sessionRepo.find(id_utente));
    }

    @Transactional
    public void addSedeNazione(String idPod, String sede, String nazione, int idUtente) {
        podRepo.aggiungiSedeNazione(idPod, sede, nazione, sessionRepo.find(idUtente));
    }

    @Transactional
    public List<Pod> findPodByIdUser(int idSessione) {
        return podRepo.findPodByIdUser(sessionRepo.find(idSessione));
    }

    @Transactional
    public List<PDFFile> getBollette(List<Pod> elencoPod) {
        return podRepo.getBollette(elencoPod);
    }

    public void addSpread(String idPod, Double spread, int idSessione) {

        int idUtente = sessionService.trovaUtentebBySessione(idSessione);
        if (idUtente != podRepo.findById(idPod).getUtente().getId()) {
            throw new NotYourPodException("Non puoi modificare il pod di un altro utente");
        }

        podRepo.aggiungiSpread(idPod, spread);
    }

    public void modificaSedeNazione(String idPod, String sede, String nazione, int idSessione) {

        int idUtente = sessionService.trovaUtentebBySessione(idSessione);
        if (idUtente != podRepo.findById(idPod).getUtente().getId()) {
            throw new NotYourPodException("Non puoi modificare il pod di un altro utente");
        }

        podRepo.modificaSedeNazione(idPod, sede, nazione);
    }
}