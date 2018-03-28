package application.applications;

import application.ADBUtil;
import application.logcat.LogCatTabController;
import application.utilities.ADB;
import application.utilities.Utilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ApplicationTabController implements Initializable {

    public static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\applications";

    private final String EXTENSION = ".apk";

    @FXML
    private TextArea resultTextArea;

    @FXML
    private Button showLogCatButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    @FXML
    private AnchorPane pane;

    @FXML
    private ListView<String> appsOnPCListView;
    @FXML
    private ListView<String> appsOnDeviceListView;

    private ObservableList<String> appsOnPCList;
    private ObservableList<String> appsOnDeviceList;

    private File directory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        directory = new File(DIRECTORY);
        resultTextArea.setEditable(false);

        initializeButtons();
        updatePCListView();
    }

    private void updatePCListView() {
        appsOnPCList = FXCollections.observableArrayList();
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            appsOnPCList.add(file.getName());
        }

        appsOnPCListView.setItems(appsOnPCList);
    }


    public void updateDeviceListView() {
        appsOnDeviceList = FXCollections.observableArrayList();
        try {
            appsOnDeviceListView.getItems().clear();
            appsOnDeviceList.clear();
            ArrayList<String> applications = ADBUtil.listApplications();
            appsOnDeviceList = FXCollections.observableArrayList(applications);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
         appsOnDeviceListView.setItems(appsOnDeviceList);
    }

    @FXML
    private void handleInstallButtonClicked(ActionEvent event) {
        String appName = appsOnPCListView.getSelectionModel().getSelectedItem();
        System.out.println(directory.getAbsolutePath() + appName);
        resultTextArea.setText(ADB.installApp(directory.getAbsolutePath() + "\\" + appName));

        updateDeviceListView();
    }

    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        System.out.println("delete");
        String fileName = appsOnPCListView.getSelectionModel().getSelectedItem();
        int fileIndex = appsOnPCListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName);
        if(fileToDelete.delete()) {
            System.out.println("File deleted");
            appsOnPCList.remove(fileIndex);

            updatePCListView();
        }
    }

    @FXML
    private void handleOpenButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(ADB.openApp(appName));
    }

    @FXML
    private void handleUninstallButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(ADB.uninstallApp(appName));
    }

    @FXML
    private void handleCopyButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(ADB.getAPK(appName, directory.getAbsolutePath()));
        updatePCListView();
    }

    @FXML
    private void handleCloseButtonClicked(ActionEvent event) {
        String appName = appsOnDeviceListView.getSelectionModel().getSelectedItem();
        resultTextArea.setText(ADB.closeApp(appName));
    }

    @FXML
    private void handleLogCatButtonClicked(ActionEvent event) {
        LogCatTabController logCatTabController = new LogCatTabController();
        try {
            logCatTabController.newWindow(this, null);
        } catch (IOException ioe) {
            resultTextArea.setText(ioe.getMessage());
        }
    }

    @FXML
    private void handleRefreshButtonClicked(ActionEvent event) {
        updateDeviceListView();
        updatePCListView();
    }

    private void initializeButtons() {
        Utilities.setImage("/resources/delete.png", "Delete file", deleteButton);
        Utilities.setImage("/resources/pop_out.png", null, showLogCatButton);
        Utilities.setImage("/resources/refresh.png", null, refreshButton);
    }
}
