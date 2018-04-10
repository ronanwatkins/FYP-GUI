package application.utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import application.location.KML;
import application.location.LocationTabController;
import application.logcat.Filter;
import application.logcat.LogCatTabController;
import application.logcat.LogLevel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {
    private Document document;
    private Element rootElement;

    /*******************************************************
     * CONSTRUCTORS
     ******************************************************/
    public XMLUtil(boolean isKML) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            document = documentBuilder.newDocument();
            rootElement = document.createElement(isKML ? "kml" : "global");
            document.appendChild(rootElement);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }

    /*******************************************************
     * SENSOR TAB FUNCTIONS
     ******************************************************/
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

    /*******************************************************
     * LOCATION TAB FUNCTIONS
     ******************************************************/
    public void updateFile(String name, ObservableList<KML> KMLCommands) {
        XMLUtil xmlUtil = new XMLUtil(true);

        for(KML kml : KMLCommands) {
            xmlUtil.addKMLElement(kml);
        }

        saveKMLFile(name);
    }

    public ObservableList<KML> openKMLCommands(String name) {
        File file = new File( LocationTabController.DIRECTORY + "\\" + name + ".kml");
        ObservableList<KML> KMLList = FXCollections.observableArrayList();

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(file);

                    document.getDocumentElement().normalize();

                    Element element;

                    NodeList nodeList = document.getElementsByTagName("kml");

                    for (int i = 0; i < nodeList.getLength(); i++) { //looping through "Document"
                        Node node = nodeList.item(i);
                        NodeList childList = node.getChildNodes();
                        for(int j=0; j<childList.getLength(); j++) { //looping through "Placemark"
                            Node childNode = childList.item(j);
                            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                                element = (Element) childNode;

                                String name = (element.getElementsByTagName("name").item(0).getTextContent());
                                String description = (element.getElementsByTagName("description").item(0).getTextContent());

                                String[] values = (element.getElementsByTagName("Point").item(0).getTextContent()).split(",");
                                Double longitude = Double.parseDouble(values[0]);
                                Double latitude = Double.parseDouble(values[1]);
                                Double altitude = Double.parseDouble(values[2]);

                                KMLList.add(new KML(name, description, latitude, longitude, altitude));
                            }
                        }
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                return null;
            }
        };
        new Thread(task).run();

        return KMLList;
    }

    public Element addKMLElement(KML kml) {
        Element placemark = document.createElement("Placemark");
        rootElement.appendChild(placemark);

        Element name = document.createElement("name");
        name.appendChild(document.createTextNode(kml.getName()));
        placemark.appendChild(name);

        Element description = document.createElement("description");
        description.appendChild(document.createTextNode(kml.getDescription()));
        placemark.appendChild(description);

        Element point = document.createElement("Point");

        Element coordinates = document.createElement("coordinates");
        coordinates.appendChild(document.createTextNode(kml.getAllValues()));
        point.appendChild(coordinates);

        placemark.appendChild(point);

        return placemark;
    }

    public void saveKMLFile(String name) {
        File file = new File( LocationTabController.DIRECTORY + "\\" + name + ".kml");

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    /*******************************************************
     * COMMON FUNCTIONS
     ******************************************************/
    public void saveFile(File file) {
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

        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    /*******************************************************
     * AUTOMATION TAB FUNCTIONS
     ******************************************************/
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
                } catch (Exception ee) {
                    ee.printStackTrace();
                }

                return null;
            }
        };
        new Thread(task).run();

        return returnList;
    }

    /*******************************************************
     * FILTER FUNCTIONS
     ******************************************************/
    private final String APPLICATION_NAME = "applicationName";
    private final String PID_TAG = "PID";
    private final String LOG_MESSAGE = "logMessage";
    private final String LOG_TAG = "logTag";
    private final String LOG_LEVEL = "logLevel";

    public void saveFilter(Filter filter) {
        Element element = document.createElement(APPLICATION_NAME);
        element.appendChild(document.createTextNode(filter.getApplicationName()));
        rootElement.appendChild(element);

        element = document.createElement(PID_TAG);
        element.appendChild(document.createTextNode(""+filter.getPID()));
        rootElement.appendChild(element);

        element = document.createElement(LOG_MESSAGE);
        element.appendChild(document.createTextNode(filter.getLogMessage()));
        rootElement.appendChild(element);

        element = document.createElement(LOG_TAG);
        element.appendChild(document.createTextNode(filter.getLogTag()));
        rootElement.appendChild(element);

        element = document.createElement(LOG_LEVEL);
        element.appendChild(document.createTextNode(filter.getLogLevel()));
        rootElement.appendChild(element);

        System.out.println("saveFilter>> " + filter.toString());
        File file = new File( LogCatTabController.FILTER_DIRECTORY + filter.getFilterName() + ".xml");
        saveFile(file);
    }

    public Filter openFilter(String name) {
        File file = new File( LogCatTabController.FILTER_DIRECTORY + name + ".xml");
        Filter filter = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            document.getDocumentElement().normalize();

            Element element;

            NodeList nodeList = document.getElementsByTagName("global");

            for (int i = 0; i < nodeList.getLength(); i++) { //looping through "global"
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element) node;

                    String applicationName = (element.getElementsByTagName(APPLICATION_NAME).item(0).getTextContent());
                    String PID = element.getElementsByTagName(PID_TAG).item(0).getTextContent();
                    String logMessage = (element.getElementsByTagName(LOG_MESSAGE).item(0).getTextContent());
                    String logTag = (element.getElementsByTagName(LOG_TAG).item(0).getTextContent());
                    String logLevel = (element.getElementsByTagName(LOG_LEVEL).item(0).getTextContent());

                    System.out.println("openFilter>> LogLevel.getOrdinal(logLevel): " + LogLevel.getOrdinal(logLevel));
                    filter = new Filter(name, applicationName, PID, logMessage, logTag, LogLevel.getOrdinal(logLevel));
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return filter;

    }
}
