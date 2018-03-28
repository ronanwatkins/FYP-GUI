package application.logcat;

import application.ADBUtil;
import application.applications.ApplicationTabController;
import application.utilities.Showable;
import application.utilities.Utilities;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

public class LogCatTabController implements Initializable, Showable<Initializable> {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> logCatListView;

    @FXML
    private ComboBox logLevelComboBox;
    @FXML
    private ComboBox filtersComboBox;

    @FXML
    private Button clearButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button addFilterButton;
    @FXML
    private Button deleteFilterButton;
    @FXML
    private Button startButton;

    private Thread getLogCatThread;
    private Task<Void> getLogCatTask;

    ApplicationTabController applicationTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        System.out.println();
        logLevelComboBox.getSelectionModel().select(0);
        filtersComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void handleStartButtonClicked(ActionEvent event) {

        if(startButton.getText().equalsIgnoreCase("start")) {
            getLogCatTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + ADBUtil.getDeviceName() + " logcat");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {


                        final String newLine = line;
                        Platform.runLater(() -> logCatListView.getItems().add(newLine));
                    }
                    return null;
                }
            };
            getLogCatThread = new Thread(getLogCatTask);
            getLogCatThread.start();

            startButton.setText("Stop");
        } else {
            startButton.setText("Start");
            logCatListView.getItems().clear();
            getLogCatTask.cancel();
            getLogCatThread.interrupt();
        }
    }

    @Override
    public void newWindow(Initializable controller, File file) throws IOException {
        if(controller instanceof ApplicationTabController)
            applicationTabController = (ApplicationTabController) controller;

        FXMLLoader fxmlLoader = new FXMLLoader(applicationTabController.getClass().getResource("/application/logcat/LogCatTab.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("LogCat");
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void addLog() {
        //Platform.runLater(() -> logCatListView.getItems().add(ADBUtil.consoleCommand("logcat")));
    }

    private void initializeButtons() {
        Utilities.setImage("/resources/save.png", "Save log file", saveButton);
        Utilities.setImage("/resources/clear.png", "Clear logcat", clearButton);
        Utilities.setImage("/resources/plus.png", "Create new filter", addFilterButton);
        Utilities.setImage("/resources/minus.png", "Delete filer", deleteFilterButton);
    }

    @FXML
    private void handleClearButtonClicked(ActionEvent event) {
        logCatListView.getItems().clear();
    }
}
