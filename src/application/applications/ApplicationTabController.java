package application.applications;

import application.device.AndroidApplication;
import application.device.Device;
import application.device.DeviceIntent;
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
    private TextField searchField;
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
    private TableView<DeviceIntent> intentsTableView;
    @FXML
    private TableColumn<DeviceIntent, String> actionColumn;
    @FXML
    private TableColumn<DeviceIntent, String> componentColumn;
    @FXML
    private TableColumn<DeviceIntent, String> categoryColumn;
    @FXML
    private TableColumn<DeviceIntent, String> intentTypeColumn;
    @FXML
    private TableColumn<DeviceIntent, String> mimeTypeColumn;

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
    private Button showLogCatButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button installButton;
    @FXML
    private Button openButton;
    @FXML
    private Button uninstallButton;
    @FXML
    private Button copyButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button sendIntentButton;

    @FXML
    private AnchorPane pane;

    @FXML
    private ListView<String> appsOnPCListView;
    @FXML
    private ListView<String> appsOnDeviceListView;

    private ObservableList<String> appsOnPCList;

    private File directory;

    private LogCatTabController logCatTabController;

    private Device device = Device.getInstance();
    private DeviceIntent selectedIntent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        directory = new File(DIRECTORY);

        initializeApplicationTableView();
        initializeIntentTableView();
        initializeComboBoxes();
        initializeButtons();
        updatePCListView();
    }

    private void initializeApplicationTableView() {
        APKNameColumn.setCellValueFactory(cellData -> cellData.getValue().APKNameProperty());
        APKPathColumn.setCellValueFactory(cellData -> cellData.getValue().APKPathProperty());
        APKPathColumn.setCellFactory(tc -> textWrappingCell());
        versionCodeColumn.setCellValueFactory(cellData -> cellData.getValue().versionCodeProperty());
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty().asObject());
        dataDirColumn.setCellValueFactory(cellData -> cellData.getValue().dataDirProperty());
        dataDirColumn.setCellFactory(tc -> textWrappingCell());
    }

    private TableCell<AndroidApplication, String> textWrappingCell() {
        TableCell<AndroidApplication, String> cell = new TableCell<>();
        Text text = new Text();
        cell.setGraphic(text);
        cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
        text.wrappingWidthProperty().bind(APKPathColumn.widthProperty());
        text.textProperty().bind(cell.itemProperty());
        return cell;
    }

    private void initializeIntentTableView() {
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().actionProperty());
        componentColumn.setCellValueFactory(cellData -> cellData.getValue().componentProperty());
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        intentTypeColumn.setCellValueFactory(cellData -> cellData.getValue().intentTypeProperty());
        mimeTypeColumn.setCellValueFactory(cellData -> cellData.getValue().isMimeTypedProperty());
    }

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

    private void updateDeviceListView() {
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

    @FXML
    private void handleOpenButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(openApp(appName));
    }

    @FXML
    private void handleUninstallButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(uninstallApp(appName));
        updateDeviceListView();
    }

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

    @FXML
    private void handleCloseButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(closeApp(appName));
    }

    @FXML
    private void handleLogCatButtonClicked(ActionEvent event) {
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

    @FXML
    private void handleRefreshButtonClicked(ActionEvent event) {
        Log.info("");

        applicationTableView.getItems().clear();
        updateDeviceListView();
        updatePCListView();
    }

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

    @FXML
    private void handleSearchFieldAction(KeyEvent event) {
        appsOnDeviceListView.setItems(filter(searchField.getText(), device.getApplicationNames()));
    }

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

    public String getApplicationName() {
        return appsOnDeviceListView.getSelectionModel().getSelectedItem();
    }

    public void setLogCatTabController(LogCatTabController logCatTabController) {
        this.logCatTabController = logCatTabController;
    }

    private void enableButtons() {
        showLogCatButton.setDisable(false);
        uninstallButton.setDisable(false);
        copyButton.setDisable(false);
    }

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

    private void initializeComboBoxes() {
        componentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                componentField.setText(newValue);
            }
        });

        mimeTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mimeTypeField.setText(newValue);
            }
        });

        schemeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dataField.setText(newValue);
            }
        });
    }


    @FXML
    private void updateMimeTypeComboBox() {
        if (selectedIntent == null)
            return;

        if(selectedIntent.takesData())
            updateSchemeComboBox();

             mimeTypeComboBox.setItems(DeviceIntent.getAssociatedMimeTypes(
                    device.getSelectedApplication().getName(),
                    componentField.getText(),
                    selectedIntent.getIntentType())
            );
    }

    @FXML
    private void updateSchemeComboBox() {
        schemeComboBox.setItems(DeviceIntent.getAssociatedSchemes(
                device.getSelectedApplication().getName(),
                componentField.getText(),
                selectedIntent.getIntentType())
        );
    }
}
