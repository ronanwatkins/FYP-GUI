package application.sensors;

import application.TelnetServer;
import application.XMLUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.event.ListDataEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
import java.util.*;

public class SensorsTabController implements Initializable {

    private final String LIGHT = "light";
    private final String ACCELEROMETER_1 = "accelerometer_1";
    private final String ACCELEROMETER_2 = "accelerometer_2";
    private final String ACCELEROMETER_3 = "accelerometer_3";
    private final String HUMIDITY = "humidity";
    private final String PRESSURE = "pressure";
    private final String MAGNETOMETER_1 = "magnetic-field_1";
    private final String MAGNETOMETER_2 = "magnetic-field_2";
    private final String MAGNETOMETER_3 = "magnetic-field_3";
    private final String PROXIMITY = "proximity";
    private final String TEMPERATURE = "temperature";
    private final String LOCATION = "location";
    private final String BATTERY = "battery";

    @FXML
    private Slider lightSlider;
    @FXML
    private Slider temperatureSlider;
    @FXML
    private Slider pressureSlider;
    @FXML
    private Slider proximitySlider;
    @FXML
    private Slider humiditySlider;

    @FXML
    private Label lightLabel;
    @FXML
    private Label temperatureLabel;
    @FXML
    private Label pressureLabel;
    @FXML
    private Label proximityLabel;
    @FXML
    private Label humidityLabel;

    @FXML
    private TextField magneticField1;
    @FXML
    private TextField magneticField2;
    @FXML
    private TextField magneticField3;

    @FXML
    private TextField accelerometerField1;
    @FXML
    private TextField accelerometerField2;
    @FXML
    private TextField accelerometerField3;

    @FXML
    private Button recordButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button loadButton;

    @FXML
    private AnchorPane anchorPane;

    private double magneticFieldVal1 = 0;
    private double magneticFieldVal2 = 0;
    private double magneticFieldVal3 = 0;

    private double accelerometerVal1 = 0;
    private double accelerometerVal2 = 0;
    private double accelerometerVal3 = 0;

    private Map<String, Double> sensorValues = new HashMap<>();

    private XMLUtil xmlUtil;

    private boolean isRecording = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        xmlUtil = new XMLUtil();
        saveButton.setDisable(true);
        initHashMap();

        lightSlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    double lightValue = newvalue.doubleValue();
                    sensorValues.put(LIGHT, lightValue);
                    lightLabel.setText(String.format("%.2f",lightValue)+"");
                    TelnetServer.setSensor("light " + String.format("%.2f",lightValue));
                } );

        temperatureSlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    double temperatureValue = newvalue.doubleValue();
                    sensorValues.put(TEMPERATURE,  temperatureValue);
                    temperatureLabel.setText(String.format("%.2f",temperatureValue)+"");
                    TelnetServer.setSensor("temperature " + String.format("%.2f",temperatureValue));
                } );

        pressureSlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    double pressureValue = newvalue.doubleValue();
                    sensorValues.put(PRESSURE, pressureValue);
                    pressureLabel.setText(String.format("%.2f",pressureValue)+"");
                    TelnetServer.setSensor("pressure " + String.format("%.2f",pressureValue));
                } );

        proximitySlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    double proximityValue = newvalue.doubleValue();
                    sensorValues.put(PROXIMITY, proximityValue);
                    proximityLabel.setText(String.format("%.2f",proximityValue)+"");
                    TelnetServer.setSensor("proximity " + String.format("%.2f",proximityValue));
                } );

        humiditySlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    double humidityValue = newvalue.doubleValue();
                    sensorValues.put(HUMIDITY, humidityValue);
                    humidityLabel.setText(String.format("%.2f",humidityValue)+"");
                    TelnetServer.setSensor("humidity " + String.format("%.2f",humidityValue));
                } );

        magneticField1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                magneticFieldVal1 = Double.parseDouble(magneticField1.getText());
                sensorValues.put(MAGNETOMETER_1, magneticFieldVal1);
                TelnetServer.setSensor("magnetic-field " + magneticFieldVal1 + ":" + magneticFieldVal2 + ":" + magneticFieldVal3);
            }
        });

        magneticField2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                magneticFieldVal2 = Double.parseDouble(magneticField2.getText());
                sensorValues.put(MAGNETOMETER_2, magneticFieldVal2);
                TelnetServer.setSensor("magnetic-field " + magneticFieldVal1 + ":" + magneticFieldVal2 + ":" + magneticFieldVal3);
            }
        });

        magneticField3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                magneticFieldVal3 = Double.parseDouble(magneticField3.getText());
                sensorValues.put(MAGNETOMETER_3, magneticFieldVal3);
                TelnetServer.setSensor("magnetic-field " + magneticFieldVal1 + ":" + magneticFieldVal2 + ":" + magneticFieldVal3);
            }
        });

        accelerometerField1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                accelerometerVal1 = Double.parseDouble(accelerometerField1.getText());
                sensorValues.put(ACCELEROMETER_1, accelerometerVal1);
                TelnetServer.setSensor("acceleration " + accelerometerVal1 + ":" + accelerometerVal2 + ":" + accelerometerVal3);
            }
        });

        accelerometerField2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                accelerometerVal2 = Double.parseDouble(accelerometerField2.getText());
                sensorValues.put(ACCELEROMETER_2, accelerometerVal2);
                TelnetServer.setSensor("acceleration " + accelerometerVal1 + ":" + accelerometerVal2 + ":" + accelerometerVal3);
            }
        });

        accelerometerField3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                accelerometerVal3 = Double.parseDouble(accelerometerField3.getText());
                sensorValues.put(ACCELEROMETER_3, accelerometerVal3);
                TelnetServer.setSensor("acceleration " + accelerometerVal1 + ":" + accelerometerVal2 + ":" + accelerometerVal3);
            }
        });

        recordButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isRecording = true;

                if(isRecording) {
                    System.out.println("isRecording: " + isRecording);
                    recordButton.setDisable(true);
                    saveButton.setDisable(false);
                }

                new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if(isRecording) {
                                xmlUtil.addElement(sensorValues);
                            }
                        }
                }, 0, 500);
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isRecording = false;

                if(!isRecording) {
                    recordButton.setDisable(false);
                    saveButton.setDisable(true);
                }

                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

                Stage stage = (Stage) anchorPane.getScene().getWindow();
                File file = fileChooser.showSaveDialog(stage);

                if(file != null)
                    xmlUtil.saveFile(file);

            }
        });

        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                isRecording = false;

                FileChooser fileChooser = new FileChooser();

                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

                Stage stage = (Stage) anchorPane.getScene().getWindow();
                File file = fileChooser.showOpenDialog(stage);

                HashMap<Integer, HashMap<String, Double>> loadedValues;
                if(file != null) {
                    System.out.println(file.getAbsolutePath());
                    loadedValues = xmlUtil.loadXML(file);

                    Task task = new Task<Void>() {

                        @Override
                        public Void call() {

                            for (Integer i : loadedValues.keySet()) {
                                for (String key : loadedValues.get(i).keySet()) {

                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (key) {
                                                case LIGHT:
                                                    lightSlider.setValue(loadedValues.get(i).get(key));
                                                    break;
                                                case PROXIMITY:
                                                    proximitySlider.setValue(loadedValues.get(i).get(key));
                                                    break;
                                                case TEMPERATURE:
                                                    temperatureSlider.setValue(loadedValues.get(i).get(key));
                                                    break;
                                                case PRESSURE:
                                                    pressureSlider.setValue(loadedValues.get(i).get(key));
                                                    break;
                                                case HUMIDITY:
                                                    humiditySlider.setValue(loadedValues.get(i).get(key));
                                                    break;
                                            }
                                            System.out.println(i + " " + key + " " + loadedValues.get(i).get(key));
                                            TelnetServer.setSensor(key + " " + loadedValues.get(i).get(key));
                                        }
                                    });
                                }

                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                    ie.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                    new Thread(task).start();

                }
            }
        });

    }

    private void initHashMap() {
        sensorValues.put(LIGHT, 0.0);
        sensorValues.put(ACCELEROMETER_1, 0.0);
        sensorValues.put(ACCELEROMETER_2, 0.0);
        sensorValues.put(ACCELEROMETER_3, 0.0);
        sensorValues.put(MAGNETOMETER_1, 0.0);
        sensorValues.put(MAGNETOMETER_2, 0.0);
        sensorValues.put(MAGNETOMETER_3, 0.0);
        sensorValues.put(HUMIDITY, 0.0);
        sensorValues.put(PRESSURE, 0.0);
        sensorValues.put(TEMPERATURE, 0.0);
        sensorValues.put(PROXIMITY, 0.0);
    }
}
