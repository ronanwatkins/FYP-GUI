package application.applications;

import application.device.AndroidApplication;
import application.device.Device;
import application.device.Intent;
import application.logcat.LogCatTabController;
import application.utilities.ADB;
import application.utilities.ApplicationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static application.utilities.ADB.*;

public class ApplicationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(ApplicationTabController.class.getName());

    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\applications";
    private final String EXTENSION = ".apk";

    @FXML
    protected TextField searchField;
    @FXML
    private TextField actionField;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField componentField;
    @FXML
    private TextField mimeTypeField;
    @FXML
    private TextField dataField;

    @FXML
    private TableView<AndroidApplication> applicationTableView;

    @FXML
    private TableColumn<AndroidApplication, String> APKNameColumn;
    @FXML
    private TableColumn<AndroidApplication, String> APKPathColumn;
    @FXML
    private TableColumn<AndroidApplication, String> versionCodeColumn;
    @FXML
    private TableColumn<AndroidApplication, Integer> userIdColumn;
    @FXML
    private TableColumn<AndroidApplication, String> dataDirColumn;

    @FXML
    private TableView<Intent> intentsTableView;
    @FXML
    private TableColumn<Intent, String> actionColumn;
    @FXML
    private TableColumn<Intent, String> componentColumn;
    @FXML
    private TableColumn<Intent, String> categoryColumn;
    @FXML
    private TableColumn<Intent, String> intentTypeColumn;
    @FXML
    private TableColumn<Intent, String> mimeTypeColumn;

    @FXML
    private ComboBox<String> mimeTypeComboBox;
    @FXML
    private ComboBox<String> componentComboBox;
    @FXML
    private ComboBox<String> intentTypeComboBox;
    @FXML
    private ComboBox<String> schemeComboBox;

    @FXML
    private TextArea resultTextArea;

    @FXML
    protected Button showLogCatButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button installButton;
    @FXML
    protected Button openButton;
    @FXML
    private Button uninstallButton;
    @FXML
    private Button copyButton;
    @FXML
    protected Button closeButton;

    @FXML
    private ListView<String> appsOnPCListView;
    @FXML
    protected ListView<String> appsOnDeviceListView;

    private ObservableList<String> appsOnPCList;

    private File directory;

    protected LogCatTabController logCatTabController;

    private Device device = Device.getInstance();
    private Intent selectedIntent;

    private static ApplicationTabController applicationTabController;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        directory = new File(DIRECTORY);

        initializeApplicationTableView();
        initializeIntentTableView();
        initializeComboBoxes();
        initializeButtons();
        updatePCListView();

        applicationTabController = this;
    }

    /**
     * Returns an instance of self attached to the FXML view
     * @return Instance of self attached to the FXML view
     */
    public static ApplicationTabController getApplicationTabController() {
        return applicationTabController;
    }

    /**
     * Initializes the ApplicationTableView
     * Sets the CellValueFactories to the Properties in the {@link AndroidApplication}
     */
    private void initializeApplicationTableView() {
        APKNameColumn.setCellValueFactory(cellData -> cellData.getValue().APKNameProperty());
        APKPathColumn.setCellValueFactory(cellData -> cellData.getValue().APKPathProperty());
        APKPathColumn.setCellFactory(tc -> textWrappingCell());
        versionCodeColumn.setCellValueFactory(cellData -> cellData.getValue().versionCodeProperty());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        dataDirColumn.setCellValueFactory(cellData -> cellData.getValue().dataDirProperty());
        dataDirColumn.setCellFactory(tc -> textWrappingCell());
    }

    /**
     * Creates a cell capable of wrapping the text inside it
     * @return Cell capable of wrapping the text inside it
     */
    private TableCell<AndroidApplication, String> textWrappingCell() {
        TableCell<AndroidApplication, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(APKPathColumn.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
    }

    /**
     * Initializes the IntentTableView
     * Sets the CellValueFactories to the Properties in the {@link Intent}
     */
    private void initializeIntentTableView() {
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        componentColumn.setCellValueFactory(cellData -> cellData.getValue().componentProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        intentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().intentTypeProperty());
        mimeTypeColumn.setCellValueFactory(cellData -> cellData.getValue().isMimeTypedProperty());
    }

    /**
     * Updates the list of APK files on the PC
     */
    private void updatePCListView() {
        appsOnPCList = FXCollections.observableArrayList();
        try {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                appsOnPCList.add(file.getName());
            }
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }

        appsOnPCListView.setItems(appsOnPCList);
    }

    /**
     * Updates the list of Android Applications on the Device
     */
    public void updateDeviceListView() {
        try {
            appsOnDeviceListView.getItems().clear();
            device.getApplicationNames().clear();

            Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
                @Override
                protected ObservableList<String> call() {
                    return FXCollections.observableArrayList(ADB.listApplications());
                }
            };
            task.setOnSucceeded(event1 -> {
                Collections.sort(task.getValue());
                appsOnDeviceListView.setItems(filter(searchField.getText(), task.getValue()));
                device.setApplicationNames(task.getValue());
            });
            task.setOnFailed(event -> Log.error(task.getException().getMessage(), task.getException()));
            new Thread(task).start();
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }
    }

    /**
     * Creates a new instance of {@link AndroidApplication} when an application is selected in the AppsListView
     * Runs the creation of the {@link AndroidApplication} Object in a background thread and displays the list
     * of it's intents in the IntentsTableView when the thread completes
     * @param mouseEvent
     */
    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
        enableButtons();
        applicationTableView.setPlaceholder(new Label("Loading Application details..."));
        String applicationName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        applicationTableView.getItems().clear();
        intentsTableView.getItems().clear();

        Task<AndroidApplication> task = new Task<AndroidApplication>() {
            @Override
            protected AndroidApplication call() {
                return new AndroidApplication(applicationName);
            }
        };
        task.setOnSucceeded(event -> {
            device.setSelectedApplication(task.getValue());
            applicationTableView.getItems().add(device.getSelectedApplication());
            intentsTableView.getItems().addAll(device.getSelectedApplication().intents());
            openButton.setDisable(!device.getSelectedApplication().canOpen());
            closeButton.setDisable(openButton.isDisable());
            intentsTableView.setPlaceholder(new Label(openButton.isDisable() ? "No intents found for Application" : ""));
        });

        new Thread(task).start();
    }

    /**
     * Installs the selected APK file onto the device
     * @param event
     */
    @FXML
    private void handleInstallButtonClicked(ActionEvent event) {
        String appName = appsOnPCListView.getSelectionModel().getSelectedItem();

        resultTextArea.setText("Attempting to install " + appName + "...");
        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return installApp(directory.getAbsolutePath() + "\\" + appName);
            }
        };
        task.setOnSucceeded(event1 -> {
            resultTextArea.setText(task.getValue());
            updateDeviceListView();
        });

        new Thread(task).start();
    }

    /**
     * Deletes the selected APK file from the device and removes it from the list
     * @param event
     */
    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        String fileName = appsOnPCListView.getSelectionModel().getSelectedItem();
        int fileIndex = appsOnPCListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName);
        if (fileToDelete.delete()) {
            appsOnPCList.remove(fileIndex);
            updatePCListView();
        }
    }

    /**
     * Opens the selected application on the device
     * @param event
     */
    @FXML
    protected void handleOpenButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(openApp(appName));
    }

    /**
     * Uninstalls the selected application from the device
     * @param event
     */
    @FXML
    private void handleUninstallButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(uninstallApp(appName));
        updateDeviceListView();
    }

    /**
     * Gets the APK file of the selected application from the device and adds it to the PCListView
     * @param event
     */
    @FXML
    private void handleCopyButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText("Getting APK file for " + appName + "...");

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return getAPKFile(appName, directory.getAbsolutePath());
            }
        };
        task.setOnSucceeded(event1 -> {
            resultTextArea.setText(task.getValue());
            updatePCListView();
        });

        new Thread(task).start();
    }

    /**
     * Closes the selected application on the device
     * @param event
     */
    @FXML
    protected void handleCloseButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(closeApp(appName));
    }

    /**
     * Creates a new instance of {@link LogCatTabController} and displays it in a new window
     * @param event
     */
    @FXML
    protected void handleLogCatButtonClicked(ActionEvent event) {
        if (logCatTabController == null) {
            logCatTabController = new LogCatTabController();
            try {
                logCatTabController = (LogCatTabController) logCatTabController.newWindow(this, null);
            } catch (IOException ioe) {
                Log.error(ioe.getMessage(), ioe);
                resultTextArea.setText(ioe.getMessage());
            }
        } else {
            logCatTabController.setSearchField(getApplicationName());
        }
    }

    /**
     * Refreshes the list of android applications installed on the device
     * @param event
     */
    @FXML
    protected void handleRefreshButtonClicked(ActionEvent event) {
        Log.info("");

        applicationTableView.getItems().clear();
        updateDeviceListView();
        updatePCListView();
    }

    /**
     * Disables the deleteButton and installButton when the APK file listView is clicked
     * and if the selected file is in face an APK file
     * @param mouseEvent
     */
    @FXML
    private void handleAppsOnPCListViewClicked(MouseEvent mouseEvent) {
        try {
            if (appsOnPCListView.getSelectionModel().getSelectedItem().endsWith(EXTENSION)) {
                deleteButton.setDisable(false);
                installButton.setDisable(false);
            }
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }
    }

    /**
     * Filters the android application ListView when text is entered to the Search Field
     * @param event
     */
    @FXML
    protected void handleSearchFieldAction(KeyEvent event) {
        appsOnDeviceListView.setItems(filter(searchField.getText(), device.getApplicationNames()));
    }

    /**
     * Sends an {@link Intent} to the device built from the text in the Intent fields
     * Runs this action in a background thread and displays the result form the device in the resultTextArea
     * @param event
     */
    @FXML
    private void handleSendIntentButtonClicked(ActionEvent event) {
        String action = actionField.getText();
        String category = categoryField.getText();
        String component = componentField.getText();
        String mimeType = mimeTypeField.getText();
        String data = dataField.getText();
        int intentType = intentTypeComboBox.getSelectionModel().getSelectedIndex();

        Task<String> task = new Task<String>() {
            @Override
            protected String call() {
                return Intent.send(action, component, category, mimeType, data, intentType);
            }
        };
        task.setOnSucceeded(event1 -> resultTextArea.setText(task.getValue()));
        task.setOnFailed(event1 -> resultTextArea.setText(task.getValue()));

        new Thread(task).start();
    }

    /**
     * Adds the data from the selected intent into the Intent text fields
     * @param mouseEvent
     */
    @FXML
    private void handleIntentsTableViewClicked(MouseEvent mouseEvent) {
        if (intentsTableView.getItems().isEmpty())
            return;

        selectedIntent = intentsTableView.getSelectionModel().getSelectedItem();
        actionField.setText(selectedIntent.actionProperty().get());

        intentTypeComboBox.getSelectionModel().select(selectedIntent.getIntentType());

        mimeTypeComboBox.setItems(null);
        schemeComboBox.setItems(null);

        String component = selectedIntent.componentProperty().get().split("\n")[0];
        if (!component.equals(componentField.getText())) {
            componentField.setText(component);
            updateMimeTypeComboBox();
        }

        componentComboBox.setItems(selectedIntent.getComponents());
        categoryField.setText(selectedIntent.categoryProperty().get());

        mimeTypeField.setText("");
        dataField.setText("");
    }

    /**
     * Returns the name of the selected Android Application from the Application ListView
     * @return
     */
    public String getApplicationName() {
        return appsOnDeviceListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Sets the instance of {@link LogCatTabController}
     * @param logCatTabController
     */
    public void setLogCatTabController(LogCatTabController logCatTabController) {
        this.logCatTabController = logCatTabController;
    }

    /**
     * Enables the buttons relating to the android application
     */
    private void enableButtons() {
        showLogCatButton.setDisable(false);
        uninstallButton.setDisable(false);
        copyButton.setDisable(false);
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
        showLogCatButton.setDisable(true);
        deleteButton.setDisable(true);
        installButton.setDisable(true);
        uninstallButton.setDisable(true);
        copyButton.setDisable(true);
        openButton.setDisable(true);
        closeButton.setDisable(true);

        setImage("/resources/delete.png", "Delete file", deleteButton);
        setImage("/resources/pop_out.png", null, showLogCatButton);
        setImage("/resources/refresh.png", null, refreshButton);
    }

    /**
     * Sets the valueProperty of each Intent ComboBox
     * When an item is selected in each ComboBox the text is added to to it's corresponding textField
     */
    private void initializeComboBoxes() {
        componentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                componentField.setText(newValue);
        });

        mimeTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                mimeTypeField.setText(newValue);
        });

        schemeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                dataField.setText(newValue);
        });
    }


    /**
     * Updates the mimeTypeComboBox when an item is selected in the Intent TableView
     */
    @FXML
    private void updateMimeTypeComboBox() {
        if (selectedIntent == null)
            return;

        if(selectedIntent.takesData())
            updateSchemeComboBox();

             mimeTypeComboBox.setItems(Intent.getAssociatedMimeTypes(
                    device.getSelectedApplication().getName(),
                    componentField.getText(),
                    selectedIntent.getIntentType())
            );
    }

    /**
     * Updates the schemeComboBox when an item is selected in the Intent TableView
     */
    @FXML
    private void updateSchemeComboBox() {
        schemeComboBox.setItems(Intent.getAssociatedSchemes(
                device.getSelectedApplication().getName(),
                componentField.getText(),
                selectedIntent.getIntentType())
        );
    }
}
