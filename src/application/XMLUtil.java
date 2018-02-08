package application;

import java.io.File;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLUtil {

    private Document document;
    private Element rootElement;

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

        Element sensor = null;

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
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            StreamResult result = new StreamResult(file);

            transformer.transform(source, result);

            System.out.println("file saved");
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }
}
