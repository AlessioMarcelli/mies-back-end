package miesgroup.mies.webdev.Service;

import jakarta.enterprise.context.ApplicationScoped;
import miesgroup.mies.webdev.Persistance.Model.Pod;
import miesgroup.mies.webdev.Persistance.Repository.PodRepo;
import miesgroup.mies.webdev.Persistance.Repository.SessionRepo;
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

@ApplicationScoped
public class PodService {

    private final PodRepo podRepo;
    private final SessionService sessionService;
    private final SessionRepo sessionRepo;

    public PodService(PodRepo podRepo, SessionService sessionService, SessionRepo sessionRepo) {
        this.podRepo = podRepo;
        this.sessionService = sessionService;
        this.sessionRepo = sessionRepo;
    }

    public String extractValuesFromXml(byte[] xmlData, int sessione) {
        ArrayList<Double> extractedValues = new ArrayList<>();
        String id_pod = "";
        int id_utente = sessionService.trovaUtentebBySessione(sessione);
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
                    } else {
                        esiste = true;
                        System.err.println("Pod già esistente" + podRepo.verificaSePodEsiste(id_pod, id_utente));
                    }
                }
            }
            creaPod(extractedValues, id_utente, id_pod);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return id_pod;
    }


    public void creaPod(ArrayList<Double> extractedValues, int id_utente, String id_pod) {
        Pod pod = new Pod();
        pod.setId_utente(id_utente);
        pod.setId(id_pod);
        pod.setTensione_Alimentazione(extractedValues.get(0));
        pod.setPotenza_Impegnata(extractedValues.get(1));
        pod.setPotenza_Disponibile(extractedValues.get(2));
        podRepo.insert(pod);
    }

    public ArrayList<Pod> tutti(int id_sessione) {
        return podRepo.findAll(sessionRepo.find(id_sessione));
    }

    public Pod getPod(String id, int id_utente) {
        return podRepo.cercaIdPod(id, sessionRepo.find(id_utente));
    }

    public void addSedeNazione(String idPod, String sede, String nazione, int idUtente) {
        podRepo.aggiungiSedeNazione(idPod, sede, nazione, sessionRepo.find(idUtente));
    }
}