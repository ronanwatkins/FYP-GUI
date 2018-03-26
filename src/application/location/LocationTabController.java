package application.location;

import application.TelnetServer;
import application.utilities.KML;
import application.utilities.XMLUtil;
import application.utilities.Utilities;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;


import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class LocationTabController implements Initializable {

    public static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\location";

    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;

    @FXML
    private GoogleMapView googleMapView;

    @FXML
    private Button sendButton;
    @FXML
    private Button addButton;
    @FXML
    private Button playButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;

    @FXML
    private ListView<String> KMLFilesListView;

    @FXML
    private TableView<KML> KMLTableView;
    @FXML
    private TableColumn<KML, String> nameColumn;
    @FXML
    private TableColumn<KML, String> descriptionColumn;
    @FXML
    private TableColumn<KML, Double> latitudeColumn;
    @FXML
    private TableColumn<KML, Double> longitudeColumn;
    @FXML
    private TableColumn<KML, Double> altitudeColumn;

    @FXML
    private ObservableList<String> KMLCommandsList;

    private File directory = null;

    private GoogleMap map;

    private DecimalFormat formatter = new DecimalFormat("###.00000");

    //Initialize to latitude of GMIT
    private double latitude = 53.278458;
    private double longitude = -9.009833;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initializeButtons();

        KMLCommandsList = FXCollections.observableArrayList();
        directory = new File(DIRECTORY);
        updateCommandsList();

        latitudeField.setText(formatter.format(latitude));
        longitudeField.setText(formatter.format(longitude));
        googleMapView.addMapInializedListener(this::configureMap);

        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        latitudeColumn.setCellValueFactory(cellData -> cellData.getValue().latitudeProperty().asObject());
        longitudeColumn.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty().asObject());
        altitudeColumn.setCellValueFactory(cellData -> cellData.getValue().altitudeProperty().asObject());
//        latitudeColumn.setCellValueFactory(cellData ->
//                Bindings.format("%.2f", cellData.getValue().latitudeProperty()));
//        latitudeColumn.setCellValueFactory(param -> {
//            param.getValue().latitudeProperty();
//            return null;
//        });
//        longitudeColumn.setCellValueFactory(param -> {
//            param.getValue().longitudeProperty();
//            return null;
//        });
//        altitudeColumn.setCellValueFactory(param -> {
//            param.getValue().altitudeProperty();
//            return null;
//        });
    }

    @FXML
    private void handleKMLFilesListViewClicked(MouseEvent event) {
        refreshCommandsList();
    }

    private void refreshCommandsList() {
        XMLUtil xmlUtil = new XMLUtil(true);

        String commandName = KMLFilesListView.getSelectionModel().getSelectedItem();
        System.out.println("Command name: " + commandName);
        if(commandName != null) {
            ObservableList<KML> kmlCommands = xmlUtil.openKMLCommands(new File(DIRECTORY + "\\" + commandName + ".kml"));
            KMLTableView.getItems().clear();

            KMLTableView.setItems(kmlCommands);
            for (KML kml : kmlCommands) {
                System.out.println(kml.toString());
//                FileContentsListView.getItems().add(kml.toString());
//                KMLTableView
            }
            playButton.setDisable(false);
            stopButton.setDisable(false);
        } else {
            //FileContentsListView.setItems(null);
            playButton.setDisable(true);
            stopButton.setDisable(true);
        }
    }

    public void updateCommandsList() {
        try {
            KMLFilesListView.getItems().clear();
            KMLCommandsList.clear();
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                KMLCommandsList.add(file.getName().replace(".kml", ""));
            }
        } catch (NullPointerException npe) {
            //npe.printStackTrace();
        }

        KMLFilesListView.setItems(KMLCommandsList);
    }

    @FXML
    private void handleSendButtonClicked(ActionEvent event) {
        TelnetServer.setLocation(longitude + " " + latitude);
    }

    @FXML
    private void handleAddButtonClicked(ActionEvent event) {

    }

    @FXML
    private void handlePlayButtonClicked(ActionEvent event) {

    }

    @FXML
    private void handleStopButtonClicked(ActionEvent event) {

    }

    @FXML
    private void handleNewButtonClicked(ActionEvent event) {

    }

    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {

    }

    private void initializeButtons() {
        playButton.setDisable(true);
        stopButton.setDisable(true);

        Utilities.setImage("/resources/play.png", null, playButton);
        Utilities.setImage("/resources/stop.png", null, stopButton);
        Utilities.setImage("/resources/new.png", "Create new KML file", newButton);
        Utilities.setImage("/resources/delete.png", "Delete KML file", deleteButton);
    }

    private void configureMap() {
        MapOptions mapOptions = new MapOptions();

        mapOptions.streetViewControl(false);

        mapOptions.center(new LatLong(latitude, longitude))
                .mapType(MapTypeIdEnum.ROADMAP)
                .zoom(9);
        map = googleMapView.createMap(mapOptions, false);

        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();

            latitude = Double.parseDouble(formatter.format(latLong.getLatitude()));
            longitude = Double.parseDouble(formatter.format(latLong.getLongitude()));

            latitudeField.setText(formatter.format(latitude));
            longitudeField.setText(formatter.format(longitude));
        });
    }
}
