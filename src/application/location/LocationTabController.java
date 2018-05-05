package application.location;

import application.location.model.KML;
import application.utilities.TelnetServer;
import application.automation.AutomationTabController;
import application.utilities.ApplicationUtils;
import application.utilities.XMLUtil;
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
import org.apache.log4j.Logger;


import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class LocationTabController extends AutomationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(LocationTabController.class.getName());

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

    private final int INTERVAL = 3000;

    private String fileName;

    private GoogleMap map;

    private DecimalFormat formatter = new DecimalFormat("###.00000");

    //Initialize to latitude of GMIT
    private double latitude = 53.278458;
    private double longitude = -9.009833;
    private double altitude = 9;

    private ArrayList<Marker> markers;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();

        filesList = FXCollections.observableArrayList();
        directory = new File(DIRECTORY);
        updateCommandsList();

        latitudeField.setText(formatter.format(latitude));
        longitudeField.setText(formatter.format(longitude));
        googleMapView.addMapInializedListener(this::configureMap);

        KMLTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        latitudeColumn.setCellValueFactory(cellData -> cellData.getValue().latitudeProperty().asObject());
        longitudeColumn.setCellValueFactory(cellData -> cellData.getValue().longitudeProperty().asObject());
        altitudeColumn.setCellValueFactory(cellData -> cellData.getValue().altitudeProperty().asObject());
    }


    /**
     * Handles the List view of KML files being clicked
     * @param event
     */
    @FXML
    private void handleKMLFilesListViewClicked(MouseEvent event) {
        refreshCommandsList();
        updateMarkers();
    }

    /**
     * Handles the Table view of KML commands being clicked
     * enables all button controls for the TableView
     * If a double clicked is detected, The map zooms to the selected location and the location is sent to the emulator
     * @param event
     */
    @FXML
    private void handleKMLTableViewClicked(MouseEvent event) {
        if(KMLTableView.getSelectionModel().getSelectedItem() == null)
            return;

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

    /**
     * Updates the list of KML commands in the TableView
     * enables all button controls for the TableView
     */
    @Override
    public void refreshCommandsList() {
        fileName = filesListView.getSelectionModel().getSelectedItem();
        if(fileName != null) {
            KMLTableView.setPlaceholder(new Label("Add waypoints to the list"));
            commandsList = KML.getKMLCommands(fileName);
            KMLTableView.getItems().clear();

            KMLTableView.setItems(commandsList);
            for (KML kml : commandsList) {
                Log.info(kml.toString());
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

    /**
     * Updates the list of KML files in the ListView
     */
    @Override
    public void updateCommandsList() {
        try {
            filesListView.getItems().clear();
            filesList.clear();
            for (File file : Objects.requireNonNull(directory.listFiles()))
                filesList.add(file.getName().replace(EXTENSION, ""));
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }

        filesListView.setItems(filesList);
    }

    /**
     * Handles the send button being clicked
     * Sends the selected coordinates to the emulator
     * @param event
     */
    @FXML
    private void handleSendButtonClicked(ActionEvent event) {
        updateValues();
        TelnetServer.setLocation(longitude + " " + latitude);
    }

    /**
     * Adds the selected coordinates, name and description to the selected KML file
     * Adds a marker to the selected location to the Map
     * @param event
     */
    @FXML
    private void handleAddButtonClicked(ActionEvent event) {
        updateValues();
        KML kml = new KML(nameTextField.getText(), descriptionTextField.getText(), latitude, longitude, altitude);

        KMLTableView.getItems().add(KMLTableView.getItems().size(), kml);

        KML.update(fileName, KMLTableView.getItems());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLong(kml.getLatitude(), kml.getLongitude() ))
                .visible(Boolean.TRUE)
                .title(kml.getName())
                .label(kml.getDescription());

        Marker marker = new Marker(markerOptions);
        markers.add(marker);
        map.addMarkers(markers);
    }

    /**
     * Deletes the selected command from the selected KML file
     * @param event
     */
    @FXML
    private void handleDeleteCommandButtonClicked(ActionEvent event) {
        int commandsListViewIndex = KMLTableView.getSelectionModel().getSelectedIndex();
        if (commandsListViewIndex > -1) {
            try {
                KMLTableView.getItems().remove(commandsListViewIndex);
                updateMarkers();
            } catch (NullPointerException npe) {
                Log.error(npe.getMessage(), npe);
            }

            ObservableList<KML> KMLCommands = KMLTableView.getItems();
            KML.update(fileName, KMLCommands);
        }
    }

    /**
     * Runs through the TableView of KML commands
     * Sends the coordinates of each command to the emulator at an interval of {@link #INTERVAL}
     * and pans the map to that location
     * @param event
     */
    @FXML
    @Override
    protected void handlePlayButtonClicked(ActionEvent event) {
        if(runCommandsTask != null) {
            if(runCommandsTask.isRunning()) {
                if (!pauseFlag.get()) {
                    synchronized (pauseFlag) {
                        pauseFlag.set(true);
                    }

                    setImage("/resources/play.png", "Run batch commands", playButton);
                } else {
                    synchronized (pauseFlag) {
                        pauseFlag.set(false);
                        pauseFlag.notify();
                    }

                    setImage("/resources/pause.png", "Pause batch commands",playButton);
                    wasPaused.set(true);
                }
            }
        }

        if(!wasPaused.get() && !pauseFlag.get()) {
            setImage("/resources/pause.png", "Pause batch commands", playButton);
            Log.info("Starting new batch automation");
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
                                        Log.info("WAITING.....");
                                        pauseFlag.wait();
                                    } catch (InterruptedException e) {
                                        Log.info("waiting.....");
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
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException ie) {
                            return null;
                        }
                    }
                    return null;
                }
            };

            runCommandsTask.setOnSucceeded(event1 -> {
                setImage("/resources/play.png","Run batch commands", playButton);
                this.stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsTask.setOnFailed(event1 -> {
                setImage("/resources/play.png","Run batch commands", playButton);
                Log.info("runCommandsTask failed, Exception: " + runCommandsTask.getException());
                this.stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsThread = new Thread(runCommandsTask);
            runCommandsThread.start();
        }
    }

    /**
     * Creates a new KML file and adds it to the list
     * Shows the user a test input dialog to enter the file name
     * @param event
     */
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
                Log.error(ie.getMessage(), ie);
            }
            updateCommandsList();
        });
    }

    /**
     * Deletes the selected KML file and remove it from the list
     * Updates the markers on the Map
     * @param event
     */
    @FXML
    protected void handleDeleteButtonClicked(ActionEvent event) {
        String fileName = filesListView.getSelectionModel().getSelectedItem();
        int fileIndex = filesListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName + EXTENSION);
        if(fileToDelete.delete()) {
            filesList.remove(fileIndex);

            refreshCommandsList();

            if(filesListView.getItems().isEmpty()) {
                KMLTableView.getItems().clear();
                deleteButton.setDisable(true);
                addButton.setDisable(true);
            }

            updateMarkers();
        }
    }

    /**
     * Moves the selected command down the TableView
     * @param event
     */
    @FXML
    private void handleMoveDownButtonClicked(ActionEvent event) {
        adjustList(1);
    }

    /**
     * Moves the selected command up the TableView
     * @param event
     */
    @FXML
    private void handleMoveUpButtonClicked(ActionEvent event) {
        adjustList(-1);
    }

    /**
     * Updates the markers on the Map with the commands from the selected KML file
     */
    private void updateMarkers() {
        if(commandsList != null) {
            if (markers != null)
                map.removeMarkers(markers);
            markers = new ArrayList<>(commandsList.size());

            for (KML kml : commandsList) {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(new LatLong(kml.getLatitude(), kml.getLongitude()))
                        .visible(Boolean.TRUE)
                        .title(kml.getName())
                        .label(kml.getDescription());

                Marker marker = new Marker(markerOptions);
                markers.add(marker);
            }
            map.addMarkers(markers);
        }
    }

    /**
     * Moves the selected command up or down the TableView
     * @param input
     */
    private void adjustList(int input) {
        int selectedItemIndex = KMLTableView.getSelectionModel().getSelectedIndex();
        try {
            if (!Objects.equals(KMLTableView.getItems().get(selectedItemIndex + input), null)) {
                KML selectedItem = KMLTableView.getSelectionModel().getSelectedItem();
                KMLTableView.getItems().remove(selectedItemIndex);
                KMLTableView.getItems().add(selectedItemIndex + input, selectedItem);
                KMLTableView.getSelectionModel().select(selectedItemIndex + input);

                ObservableList<KML> KMLCommands = KMLTableView.getItems();
                KML.update(fileName, KMLCommands);
            }
        } catch (IndexOutOfBoundsException ignored) {}
    }

    /**
     * Parses the text from the latitude and longitude fields and
     * edits the global latitude and longitude double values
     */
    private void updateValues() {
        try {
            latitude = Double.parseDouble(latitudeField.getText());
            longitude = Double.parseDouble(longitudeField.getText());
        } catch (NumberFormatException nfe) {
            Log.error(nfe.getMessage(), nfe);
        }
    }

    /**
     * Initialize the buttons
     * Can do any of the following:
     * Set tooltip text
     * Set image
     * Set disabled / enabled
     * Set visible / invisible
     */
    @Override
    public void initializeButtons() {
        runTypeComboBox.getSelectionModel().select(0);
        playButton.setDisable(true);
        stopButton.setDisable(true);
        addButton.setDisable(true);
        moveDownButton.setDisable(true);
        moveUpButton.setDisable(true);
        deleteCommandButton.setDisable(true);
        deleteButton.setDisable(true);

        setImage("/resources/up.png", "Move command up the list", moveUpButton);
        setImage("/resources/down.png","Move command down the list", moveDownButton);
        setImage("/resources/delete.png", "Delete command", deleteCommandButton);

        setImage("/resources/play.png", null, playButton);
        setImage("/resources/stop.png", null, stopButton);
        setImage("/resources/new.png", "Create new KML file", newButton);
        setImage("/resources/delete.png", "Delete KML file", deleteButton);
    }

    /**
     * Configures the GoogleMap
     */
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

            latitude = Double.parseDouble(formatter.format(latLong.getLatitude()));
            longitude = Double.parseDouble(formatter.format(latLong.getLongitude()));

            latitudeField.setText(formatter.format(latitude));
            longitudeField.setText(formatter.format(longitude));
        });
    }
}
