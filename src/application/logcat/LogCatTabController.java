package application.logcat;

import application.ADBUtil;
import application.applications.ApplicationTabController;
import application.utilities.ApplicationUtils;
import application.utilities.Showable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class LogCatTabController implements Initializable, Showable<Initializable>, ApplicationUtils {
    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\logcat";
    private final String EXTENSION = ".log";

    @FXML
    private volatile TextField searchField;

    @FXML
    private volatile ListView<String> logCatListView;

    @FXML
    private ComboBox<String> logLevelComboBox;
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

    private volatile ObservableList<String> logList;

    private Thread getLogCatThread;
    private Task<Void> getLogCatTask;

    private volatile LogLevel logLevel;
    private String level;

    ApplicationTabController applicationTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        filtersComboBox.getSelectionModel().select(0);
        logList = FXCollections.observableArrayList();
        logLevel = LogLevel.NONE;

        if(resources != null) {
            searchField.setText(resources.toString());
            startButton.fire();
        }
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
                        Platform.runLater(() -> {
                            logList.add(newLine);
                            logCatListView.setItems(filter(searchField.getText(), level, logList));
                        });
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
        if(controller instanceof ApplicationTabController) {
            applicationTabController = (ApplicationTabController) controller;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(applicationTabController.getClass().getResource("/application/logcat/LogCatTab.fxml"));
        Bundle bundle = new Bundle(applicationTabController.getApplicationName());
        fxmlLoader.setResources(bundle);

        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("LogCat");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleClearButtonClicked(ActionEvent event) {
        logCatListView.getItems().clear();
    }

    private void addLog() {
        //Platform.runLater(() -> logCatListView.getItems().add(ADBUtil.consoleCommand("logcat")));
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/save.png", "Save log file", saveButton);
        setImage("/resources/plus.png", "Create new filter", addFilterButton);
        setImage("/resources/minus.png", "Delete filer", deleteFilterButton);
    }

    @FXML
    private void handleSearchFieldAction(KeyEvent keyEvent) {
        logCatListView.setItems(filter(searchField.getText(), level, logList));
    }

    @FXML
    private void handleLogLevelComboBoxPressed(ActionEvent event) {
        logLevel = LogLevel.getLogLevel(logLevelComboBox.getSelectionModel().getSelectedIndex());
        level = logLevel.toString().substring(0, 1);
        logCatListView.setItems(filter(searchField.getText(), level, logList));
    }

    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                File directory = new File(DIRECTORY);

                String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
                File file = new File(directory, ADBUtil.getDeviceName() + " " + timeStamp);
                try(PrintWriter fileWriter = new PrintWriter(file)) {
                    for (String line : logCatListView.getItems()) {
                        fileWriter.println(line);
                    }
                } catch (FileNotFoundException ee) {
                    ee.printStackTrace();
                }
                return null;
            }
        };
        task.setOnSucceeded(event1 -> {

        });

        new Thread(task).start();
    }
}
