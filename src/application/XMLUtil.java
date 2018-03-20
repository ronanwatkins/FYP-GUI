package application;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLUtil {
    private Document document;
    private Element rootElement;
    private AtomicBoolean isFileSaved = new AtomicBoolean(true);

    public XMLUtil() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            document = documentBuilder.newDocument();
            rootElement = document.createElement("global");
            document.appendChild(rootElement);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    public void addElement(Map<String, Double> sensorValues) {
        Element stage = document.createElement("stage");
        rootElement.appendChild(stage);

        Element sensor;

        for(String key : sensorValues.keySet()) {
            sensor = document.createElement("sensor");
            sensor.setAttribute("type", key);

            Element value = document.createElement("value");
            value.appendChild(document.createTextNode(sensorValues.get(key)+""));
            sensor.appendChild(value);

            stage.appendChild(sensor);
        }
    }

    public void saveFile(File file) {
        isFileSaved.set(false);

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            StreamResult result = new StreamResult(file);

            Task task = new Task<Void>() {

                @Override
                public Void call() throws TransformerException {
                        transformer.transform(source, result);
                        return null;
                }
            };
            new Thread(task).start();

            task.setOnSucceeded(event -> {
                System.out.println("file saved");
                isFileSaved.set(true);
            });

            task.setOnFailed(event -> {
                System.out.println("file not saved");
                isFileSaved.set(false);
            });
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    public HashMap<Integer, HashMap<String, Double>> loadXML(File file) {
        HashMap<Integer, HashMap<String, Double>> returnMap = new HashMap<>();

        Task task = new Task<Void>() {

            @Override
            public Void call() {

                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(file);

                    document.getDocumentElement().normalize();

                    Element element;
                    String valueString;

                    NodeList nodeList = document.getElementsByTagName("stage");

                    HashMap<String, Double> sensorValues;

                    for (int i = 0; i < nodeList.getLength(); i++) { //looping through "stage"
                        Node node = nodeList.item(i);
                        sensorValues = new HashMap<>();
                        NodeList childList = node.getChildNodes();
                        for(int j=0; j<childList.getLength(); j++) { //looping through "sensor"
                            Node childNode = childList.item(j);

                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) childNode;

                                String type = element.getAttribute("type");
                                Double value = Double.parseDouble(element.getElementsByTagName("value").item(0).getTextContent());
                                valueString = String.format("%.2f", value);

                                sensorValues.put(type, Double.parseDouble(valueString));
                            }
                        }

                        if(i>0)
                            returnMap.put(i, sensorValues);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        new Thread(task).run();

        return returnMap;
    }

    public void saveBatchCommands(ObservableList<String> batchCommands, File file) {
        Element command;

        for(String s : batchCommands) {
            command = document.createElement("command");
            command.appendChild(document.createTextNode(s));
            rootElement.appendChild(command);
        }

        saveFile(file);
    }

    public ObservableList<String> openBatchCommands(File file) {
        while (!isFileSaved.get()) {
            System.out.println("Waiting for file to be saved");
        }

        ObservableList<String> returnList = FXCollections.observableArrayList();

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(file);

                    document.getDocumentElement().normalize();

                    Element element;

                    NodeList nodeList = document.getElementsByTagName("command");

                    for (int i = 0; i < nodeList.getLength(); i++) { //looping through "command"
                        element = (Element) nodeList.item(i);
                        returnList.add(element.getTextContent());
                    }
                } catch (SAXException spe) {
                    //spe.printStackTrace();
                    System.out.println("spe FileName: " + file.getAbsolutePath());
                    //openBatchCommands(file);
                } catch (ParserConfigurationException|IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        new Thread(task).run();

        return returnList;
    }
}
