package application.location;

import application.TelnetServer;
import application.automation.AutomationTabController;
import application.utilities.XMLUtil;
import application.utilities.Utilities;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class LocationTabController extends AutomationTabController implements Initializable {

    public static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\location";

    private final String EXTENSION = ".kml";

    @FXML
    private AnchorPane pane;

    @FXML
    private TextField nameTextField;
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;

    @FXML
    private GoogleMapView googleMapView;

    @FXML
    private Button moveDownButton;
    @FXML
    private Button moveUpButton;
    @FXML
    private Button deleteCommandButton;
    @FXML
    private Button addButton;

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

    private ObservableList<KML> commandsList;

    private File KMLFile;

    private GoogleMap map;

    private DecimalFormat formatter = new DecimalFormat("###.00000");

    //Initialize to latitude of GMIT
    private double latitude = 53.278458;
    private double longitude = -9.009833;
    private double altitude = 9;

    private Collection<Marker> markers;

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        initializeButtons();

        filesList = FXCollections.observableArrayList();
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
    }

    @FXML
    private void handleKMLFilesListViewClicked(MouseEvent event) {
        refreshCommandsList();
        updateMarkers();
    }

    @FXML
    private void handleKMLTableViewClicked(MouseEvent event) {
        if(KMLTableView.getSelectionModel().getSelectedItem() != null) {
            moveDownButton.setDisable(false);
            moveUpButton.setDisable(false);
            deleteCommandButton.setDisable(false);

            if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                KML kml =  KMLTableView.getSelectionModel().getSelectedItem();
                map.panTo(new LatLong(kml.getLatitude(), kml.getLongitude()));
                map.setZoom((int) kml.getAltitude());
                TelnetServer.setLocation(kml.getCoordinate());
            }
        }
    }

    @Override
    public void refreshCommandsList() {
        XMLUtil xmlUtil = new XMLUtil(true);

        String commandName = filesListView.getSelectionModel().getSelectedItem();
        if(commandName != null) {
            KMLTableView.setPlaceholder(new Label("Add waypoints to the list"));
            KMLFile = new File(DIRECTORY + "\\" + commandName + EXTENSION);
            commandsList = xmlUtil.openKMLCommands(KMLFile);
            KMLTableView.getItems().clear();

            KMLTableView.setItems(commandsList);
            for (KML kml : commandsList) {
                System.out.println(kml.toString());
            }

            deleteButton.setDisable(false);
            addButton.setDisable(false);
            playButton.setDisable(false);
            stopButton.setDisable(false);
        } else {
            playButton.setDisable(true);
            stopButton.setDisable(true);
        }
    }

    @Override
    public void updateCommandsList() {
        try {
            filesListView.getItems().clear();
            filesList.clear();
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                filesList.add(file.getName().replace(EXTENSION, ""));
            }
        } catch (NullPointerException npe) {
            //npe.printStackTrace();
        }

        filesListView.setItems(filesList);
    }

    @FXML
    private void handleSendButtonClicked(ActionEvent event) {
        updateValues();
        TelnetServer.setLocation(longitude + " " + latitude);
    }

    @FXML
    private void handleAddButtonClicked(ActionEvent event) {
        updateValues();
        KML kml = new KML(nameTextField.getText(), descriptionTextField.getText(), latitude, longitude, altitude);

        KMLTableView.getItems().add(KMLTableView.getItems().size(), kml);

        XMLUtil xmlUtil = new XMLUtil(true);
        xmlUtil.updateFile(KMLFile, KMLTableView.getItems());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLong(kml.getLatitude(), kml.getLongitude() ))
                .visible(Boolean.TRUE)
                .title(kml.getName())
                .label(kml.getDescription());

        Marker marker = new Marker(markerOptions);
        markers.add(marker);
        map.addMarkers(markers);
    }

    @FXML
    private void handleDeleteCommandButtonClicked(ActionEvent event) {
        int commandsListViewIndex = KMLTableView.getSelectionModel().getSelectedIndex();
        if (commandsListViewIndex > -1) {
            try {
                KMLTableView.getItems().remove(commandsListViewIndex);
                updateMarkers();
            } catch (NullPointerException ignored) {}

            ObservableList<KML> KMLCommands = KMLTableView.getItems();
            XMLUtil xmlUtil = new XMLUtil(true);
            xmlUtil.updateFile(KMLFile, KMLCommands);
        }
    }

    @FXML
    @Override
    protected void handlePlayButtonClicked(ActionEvent event) {
        if(runCommandsTask != null) {
            if(runCommandsTask.isRunning()) {
                if (!pauseFlag.get()) {
                    synchronized (pauseFlag) {
                        pauseFlag.set(true);
                    }

                    Utilities.setImage("/resources/play.png", "Run batch commands", playButton);
                } else {
                    synchronized (pauseFlag) {
                        pauseFlag.set(false);
                        pauseFlag.notify();
                    }

                    Utilities.setImage("/resources/pause.png", "Pause batch commands",playButton);
                    wasPaused.set(true);
                }
            }
        }

        if(!wasPaused.get() && !pauseFlag.get()) {
            Utilities.setImage("/resources/pause.png", "Pause batch commands", playButton);
            System.out.println("Starting new batch automation");
            this.stopButton.setDisable(false);

            ObservableList<KML> KMLCommands = KMLTableView.getItems();

            int startIndex;
            if(runTypeComboBox.getSelectionModel().isSelected(1) || runTypeComboBox.getSelectionModel().isSelected(2))
                startIndex = KMLTableView.getSelectionModel().getSelectedIndex();
            else
                startIndex = 0;

            int endIndex;
            if(runTypeComboBox.getSelectionModel().isSelected(2))
                endIndex = startIndex+1;
            else
                endIndex = KMLTableView.getItems().size();

            runCommandsTask = new Task<Void>() {
                @Override
                protected Void call() {
                    int index = startIndex;
                    for (KML kml : KMLCommands.subList(startIndex, endIndex)) {

                        if (pauseFlag.get()) {
                            synchronized (pauseFlag) {
                                while (pauseFlag.get()) {
                                    try {
                                        System.out.println("WAITING.....");
                                        pauseFlag.wait();
                                    } catch (InterruptedException e) {
                                        System.out.println("waiting.....");
                                        Thread.currentThread().interrupt();
                                        return null;
                                    }
                                }
                            }
                        }

                        TelnetServer.setLocation(kml.getCoordinate());

                        final int newIndex = index++;

                        Platform.runLater(() -> {
                            KMLTableView.getSelectionModel().select(newIndex);

                            if (KMLTableView.getSelectionModel().getSelectedIndex() > 5) {
                                KMLTableView.scrollTo(KMLTableView.getSelectionModel().getSelectedIndex());
                            }

                            map.panTo(new LatLong(kml.getLatitude(), kml.getLongitude()));
                            map.setZoom((int) kml.getAltitude());
                            KMLTableView.getSelectionModel().select(newIndex);
                        });
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ie) {
                            return null;
                        }
                    }
                    return null;
                }
            };

            runCommandsTask.setOnSucceeded(event1 -> {
                Utilities.setImage("/resources/play.png","Run batch commands", playButton);
                this.stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsTask.setOnFailed(event1 -> {
                Utilities.setImage("/resources/play.png","Run batch commands", playButton);
                System.out.println("runCommandsTask failed, Exception: " + runCommandsTask.getException());
                this.stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsThread = new Thread(runCommandsTask);
            runCommandsThread.start();
        }
    }

    @FXML
    @Override
    protected void handleNewButtonClicked(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter file name");
        dialog.setHeaderText("Enter file name");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            File file = new File(DIRECTORY + "\\" + name + EXTENSION);

            XMLUtil xmlUtil = new XMLUtil(true);
            xmlUtil.saveFile(file);

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            updateCommandsList();
        });
    }

    @FXML
    protected void handleDeleteButtonClicked(ActionEvent event) {
        String fileName = filesListView.getSelectionModel().getSelectedItem();
        int fileIndex = filesListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName + EXTENSION);
        if(fileToDelete.delete()) {
            System.out.println("File deleted");
            filesList.remove(fileIndex);

            refreshCommandsList();
        }
    }

    @FXML
    private void handleMoveDownButtonClicked(ActionEvent event) {
        adjustList(1);
    }

    @FXML
    private void handleMoveUpButtonClicked(ActionEvent event) {
        adjustList(-1);
    }

    private void updateMarkers() {
        if(markers != null) map.removeMarkers(markers);
        markers = new ArrayList<>(commandsList.size());

        for(KML kml : commandsList) {
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(new LatLong(kml.getLatitude(), kml.getLongitude() ))
                    .visible(Boolean.TRUE)
                    .title(kml.getName())
                    .label(kml.getDescription());

            Marker marker = new Marker(markerOptions);
            markers.add(marker);
        }
        map.addMarkers(markers);
    }

    private void adjustList(int input) {
        int selectedItemIndex = KMLTableView.getSelectionModel().getSelectedIndex();
        try {
            if (!Objects.equals(KMLTableView.getItems().get(selectedItemIndex + input), null)) {
                KML selectedItem = KMLTableView.getSelectionModel().getSelectedItem();
                KMLTableView.getItems().remove(selectedItemIndex);
                KMLTableView.getItems().add(selectedItemIndex + input, selectedItem);
                KMLTableView.getSelectionModel().select(selectedItemIndex + input);

                ObservableList<KML> KMLCommands = KMLTableView.getItems();
                XMLUtil xmlUtil = new XMLUtil(true);
                xmlUtil.updateFile(KMLFile, KMLCommands);
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }

    private void updateValues() {
        latitude = Double.parseDouble(latitudeField.getText());
        longitude = Double.parseDouble(longitudeField.getText());
    }

    @Override
    protected void initializeButtons() {
        runTypeComboBox.getSelectionModel().select(0);
        playButton.setDisable(true);
        stopButton.setDisable(true);
        addButton.setDisable(true);
        moveDownButton.setDisable(true);
        moveUpButton.setDisable(true);
        deleteCommandButton.setDisable(true);
        deleteButton.setDisable(true);

        Utilities.setImage("/resources/up.png", "Move command up the list", moveUpButton);
        Utilities.setImage("/resources/down.png","Move command down the list", moveDownButton);
        Utilities.setImage("/resources/delete.png", "Delete command", deleteCommandButton);

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
                .zoom(altitude);
        map = googleMapView.createMap(mapOptions, false);

        map.addMouseEventHandler(UIEventType.click, (GMapMouseEvent event) -> {
            LatLong latLong = event.getLatLong();

            altitude = map.getZoom();
            System.out.println("altitude: " + altitude);

            latitude = Double.parseDouble(formatter.format(latLong.getLatitude()));
            longitude = Double.parseDouble(formatter.format(latLong.getLongitude()));

            latitudeField.setText(formatter.format(latitude));
            longitudeField.setText(formatter.format(longitude));
        });
    }
}
