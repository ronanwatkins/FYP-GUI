package application.logcat;

import application.ADBUtil;
import application.applications.ApplicationTabController;
import application.utilities.ApplicationUtils;
import application.utilities.Showable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class LogCatTabController implements Initializable, Showable<Initializable>, ApplicationUtils {
    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\logcat";
    public static final String FILTER_DIRECTORY = System.getProperty("user.dir") + "\\misc\\logcat\\filters\\";
    private final String EXTENSION = ".log";

    @FXML
    private volatile TextField searchField;
    @FXML
    private TextField resultField;

    @FXML
    private volatile ListView<String> logCatListView;

    @FXML
    private ComboBox<String> logLevelComboBox;
    @FXML
    private ComboBox<String> filtersComboBox;

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
    @FXML
    private Button editFilterButton;

    private String fileToEdit;

    private volatile ObservableList<String> logList;

    private volatile boolean stopFlag = false;

    private volatile LogLevel logLevel;

    private ApplicationTabController applicationTabController;

    private int selectedFilterIndex;

    private Filter filter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        initializeComboBox();
        logList = FXCollections.observableArrayList();
        logLevel = LogLevel.NONE;

        if(resources != null) {
            searchField.setText(resources.toString());
            startButton.fire();

            //resources.
        }

        filter = new Filter(searchField.getText(), logLevel,  new Filter());

        final HashMap<String, PseudoClass> pseudoClassHashMap = new HashMap<>();
        pseudoClassHashMap.put("V", PseudoClass.getPseudoClass("Verbose"));
        pseudoClassHashMap.put("D", PseudoClass.getPseudoClass("Debug"));
        pseudoClassHashMap.put("I", PseudoClass.getPseudoClass("Info"));
        pseudoClassHashMap.put("W", PseudoClass.getPseudoClass("Warn"));
        pseudoClassHashMap.put("A", PseudoClass.getPseudoClass("Assert"));
        pseudoClassHashMap.put("E", PseudoClass.getPseudoClass("Error"));

//        logCatListView.setCellFactory(lv -> new ListCell<String>() {
//            @Override
//            protected void updateItem(String string, boolean empty) {
//                if(string == null)
//                    return;
//
//                string = string.replace("  ", " ");
//                if(level == null || level.startsWith("N")) {
//                    if (string.contains(searchField.getText())) {
//                        super.updateItem(string, empty);
//                        setText(string);
//                    } else setText(null);
//                } else {
//                    if (!string.startsWith("-") && string.split(" ")[4].equals(level) && string.contains(searchField.getText())) {
//                        super.updateItem(string, empty);
//                        setText(string);
//                    }else setText(null);
//                }
//
//                for(PseudoClass pseudoClass : pseudoClassHashMap.values())
//                    pseudoClassStateChanged(pseudoClass, false);
//
//                if(!string.startsWith("-")) {
//                    String level = string.split(" ")[4];
//                    pseudoClassStateChanged(pseudoClassHashMap.get(level), true);
//                }
//            }
//        });
    }

    private void initializeComboBox() {
        File directory = new File(FILTER_DIRECTORY);

        filtersComboBox.getItems().clear();
        filtersComboBox.getItems().add("No filter");

        try {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                filtersComboBox.getItems().add(file.getName().replace(".xml", ""));
            }
        } catch (NullPointerException ignored) {}
    }

    @FXML
    private void handleStartButtonClicked(ActionEvent event) {
        if(startButton.getText().equalsIgnoreCase("start")) {
            logCatListView.getItems().clear();
            logList.clear();
            stopFlag = false;

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + ADBUtil.getDeviceName() + " logcat");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(stopFlag)
                            break;

                        if(isOK(filter, line)) {
                            logList.add(line);
                            final String newLine = line;
                            Platform.runLater(() -> logCatListView.getItems().add(newLine));
                        }

//                        Task<ObservableList<String>> task = new Task<ObservableList<String>>() {
//                            @Override
//                            protected ObservableList<String> call() {
//                                return filter(filter, logList);
//                            }
//                        };
//                        task.setOnSucceeded(e -> Platform.runLater(() -> logCatListView.setItems(task.getValue())));
//
//                        new Thread(task).start();
                    }
                    return null;
                }
            };

            new Thread(task).start();
            startButton.setText("Stop");
        } else {
            startButton.setText("Start");
            stopFlag = true;
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
        root.getStylesheets().add("/application/global.css");

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("LogCat");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleClearButtonClicked(ActionEvent event) {
        logList.clear();
        logCatListView.getItems().clear();
    }

    @Override
    public void initializeButtons() {
        deleteFilterButton.setDisable(true);
        editFilterButton.setDisable(true);
        setImage("/resources/save.png", "Save log file", saveButton);
        setImage("/resources/plus.png", "Create new filter", addFilterButton);
        setImage("/resources/minus.png", "Delete filer", deleteFilterButton);
        setImage("/resources/edit.png", "Edit filer", editFilterButton);
    }

    @FXML
    private void handleSearchFieldAction(KeyEvent keyEvent) {
        filter.setSearchText(searchField.getText());
        logCatListView.setItems(filter(filter, logList));
    }

    @FXML
    private void handleLogLevelComboBoxPressed(ActionEvent event) {
        logLevel = LogLevel.getLogLevel(logLevelComboBox.getSelectionModel().getSelectedIndex());

        filter.setLogLevel2(logLevel);
        logCatListView.getItems().clear();
        logCatListView.setItems(filter(filter, logList));
    }

    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        final String fileName = ADBUtil.getDeviceName() + "_" + timeStamp + EXTENSION;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                File directory = new File(DIRECTORY);
                File file = new File(directory, fileName);
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
        task.setOnSucceeded(event1 -> resultField.setText(fileName + " saved to " + DIRECTORY));

        new Thread(task).start();
    }

    @FXML
    private void handleAddFilterButtonClicked(ActionEvent event) {
        fileToEdit = "";

        CreateFilterController createFilterController = new CreateFilterController();
        try {
            createFilterController.newWindow(this, null);
        } catch (IOException ioe) {
            resultField.setText(ioe.getMessage());
        }
    }

    @FXML
    private void handleDeleteFilterButtonClicked(ActionEvent event) {
        String fileName = filtersComboBox.getSelectionModel().getSelectedItem();
        int fileIndex = filtersComboBox.getSelectionModel().getSelectedIndex();

        File fileToDelete = new File(FILTER_DIRECTORY + "\\" + fileName + ".xml");
        if(fileToDelete.delete()) {
            filtersComboBox.getItems().remove(fileIndex);
        }
    }

    public void updateFiltersComboBox() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        initializeComboBox();

        filtersComboBox.getSelectionModel().select(selectedFilterIndex);
    }

    @FXML
    private void handleFiltersComboBoxPressed(ActionEvent event) {
        if(filtersComboBox.getSelectionModel().getSelectedIndex() > -1) {
            selectedFilterIndex = filtersComboBox.getSelectionModel().getSelectedIndex();

            if (selectedFilterIndex > 0) {
                filter = Filter.getFilter(filtersComboBox.getSelectionModel().getSelectedItem());
                filter.setSearchText(searchField.getText());
                filter.setLogLevel2(logLevel);
            } else {
                filter = new Filter(searchField.getText(), logLevel,  new Filter());
            }

            deleteFilterButton.setDisable(!(selectedFilterIndex > 0));
            editFilterButton.setDisable(!(selectedFilterIndex > 0));
            logCatListView.getItems().clear();
            logCatListView.setItems(filter(filter, logList));
        } 
    }


    @FXML
    private void handleEditFilterButtonClicked(ActionEvent event) {
        fileToEdit = filtersComboBox.getSelectionModel().getSelectedItem();

        CreateFilterController createFilterController = new CreateFilterController();
        try {
            createFilterController.newWindow(this, null);
        } catch (IOException ioe) {
            resultField.setText(ioe.getMessage());
        }
    }

    public String getFileToEditName() {
        return fileToEdit;
    }
}
