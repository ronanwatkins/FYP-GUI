package application.logcat;

import application.utilities.ADBUtil;
import application.applications.ApplicationTabController;
import application.automation.AutomationTabController;
import application.utilities.ApplicationUtils;
import application.device.Device;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.ResourceBundle;

public class LogCatTabController implements Initializable, Showable<Initializable>, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(LogCatTabController.class.getName());

    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\logcat";
    public static final String FILTER_DIRECTORY = System.getProperty("user.dir") + "\\misc\\logcat\\filters\\";
    private final String EXTENSION = ".log";

    @FXML
    protected TextField searchField;
    @FXML
    private TextField resultField;

    @FXML
    private volatile ListView<String> logCatListView;

    @FXML
    private ComboBox<String> logLevelComboBox;
    @FXML
    private ComboBox<String> filtersComboBox;

    @FXML
    private Button saveButton;
    @FXML
    private Button addFilterButton;
    @FXML
    private Button deleteFilterButton;
    @FXML
    protected Button startButton;
    @FXML
    private Button editFilterButton;
    @FXML
    protected Button clearButton;

    private String fileToEdit;

    private volatile ObservableList<String> logList;

    protected volatile boolean stopFlag = false;

    private LogLevel logLevel;

    private ApplicationTabController applicationTabController;

    private int selectedFilterIndex;

    private volatile Filter filter;

    private String searchFieldText;

    protected final Object lock = new Object();

    private Device device = Device.getInstance();

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
        initializeComboBox();
        logList = FXCollections.observableArrayList();
        logLevel = LogLevel.NONE;

        if(resources != null) {
            searchField.setText(resources.toString());
            getLogs(true);
        } else
            searchField.setText("");

        filter = new Filter(searchField.getText(), logLevel);

        final HashMap<String, PseudoClass> pseudoClassHashMap = new HashMap<>();
        pseudoClassHashMap.put("V", PseudoClass.getPseudoClass("Verbose"));
        pseudoClassHashMap.put("D", PseudoClass.getPseudoClass("Debug"));
        pseudoClassHashMap.put("I", PseudoClass.getPseudoClass("Info"));
        pseudoClassHashMap.put("W", PseudoClass.getPseudoClass("Warn"));
        pseudoClassHashMap.put("A", PseudoClass.getPseudoClass("Assert"));
        pseudoClassHashMap.put("E", PseudoClass.getPseudoClass("Error"));

        logCatListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String string, boolean empty) {
                super.updateItem(string, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(string);
                }
                if(string == null)
                    return;

                for(PseudoClass pseudoClass : pseudoClassHashMap.values()) {
                    pseudoClassStateChanged(pseudoClass, false);
                }

                if (!string.startsWith("-")) {
                    string = string.replaceAll(" {2,3}", " ");
                    String level = string.split(" ")[4];
                    pseudoClassStateChanged(pseudoClassHashMap.get(level), true);
                }
            }
        });
    }

    @Override
    public Initializable newWindow(Initializable controller, Object object) throws IOException {
        applicationTabController = (ApplicationTabController) controller;

        FXMLLoader fxmlLoader = new FXMLLoader(LogCatTabController.class.getClass().getResource("/application/logcat/LogCatTab.fxml"));

        if(!(controller instanceof AutomationTabController)) {
            Bundle bundle = new Bundle(applicationTabController.getApplicationName());
            Log.info(applicationTabController.getApplicationName());
            fxmlLoader.setResources(bundle);
        }

        Parent root = fxmlLoader.load();

        LogCatTabController logCatTabController = fxmlLoader.getController();
        root.getStylesheets().add("/application/main/global.css");

        Stage stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.setTitle("LogCat");
        stage.setScene(new Scene(root,950, 600));

        stage.setOnCloseRequest(event -> applicationTabController.setLogCatTabController(null));

        stage.show();

        return logCatTabController;
    }

    /**
     * Sets the text in the searchField to the parameter
     * @param text text to put into searchField
     */
    public void setSearchField(String text) {
        searchField.setText(text);
        searchTextChanged();
    }

    /**
     * Initialize Filter ComboBox
     * Adds all files from the filter directory to the ComboBox
     */
    private void initializeComboBox() {
        File directory = new File(FILTER_DIRECTORY);

        filtersComboBox.getItems().clear();
        filtersComboBox.getItems().add("No filter");

        try {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                filtersComboBox.getItems().add(file.getName().replace(".xml", ""));
            }
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }
    }

    /**
     * Gets / stops getting logs from the device
     * @param flag flag to device to get/stop getting logs
     */
    public void getLogs(boolean flag) {
        if(flag) {
            Log.info(ADBUtil.getAdbPath() + " -s " + device.getName() + " logcat -v threadtime");

            logCatListView.getItems().clear();
            synchronized (lock) {
                logList.clear();
            }
            stopFlag = false;

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " logcat -v threadtime");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if(stopFlag)
                            break;

                        synchronized (lock) {
                            logList.add(line);
                        }

                        if(matchesFilter(filter, line)) {
                            final String newLine = line;
                            Platform.runLater(() -> logCatListView.getItems().add(newLine));
                        }
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

    /**
     * Handle start button action
     * If we are receiving logs: stop getting logs
     * If we aren't receiving logs: start getting logs
     * @param event
     */
    @FXML
    protected void handleStartButtonClicked(MouseEvent event) {
        if(startButton.getText().equalsIgnoreCase("start"))
            getLogs(true);
        else
            getLogs(false);
    }

    /**
     * Clears the logListView and logList
     * @param event
     */
    @FXML
    private void handleClearButtonClicked(ActionEvent event) {
        synchronized (lock) {
            logList.clear();
        }
        logCatListView.getItems().clear();
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
        deleteFilterButton.setDisable(true);
        editFilterButton.setDisable(true);
        setImage("/resources/save.png", "Save log file", saveButton);
        setImage("/resources/plus.png", "Create new filter", addFilterButton);
        setImage("/resources/minus.png", "Delete filer", deleteFilterButton);
        setImage("/resources/edit.png", "Edit filer", editFilterButton);
    }

    /**
     * Called when text is entered to the search field
     * @param keyEvent
     */
    @FXML
    protected void handleSearchFieldAction(KeyEvent keyEvent) {
        searchTextChanged();
    }

    /**
     * Adds the searchField text to the filter and filters the logListView
     */
    private void searchTextChanged() {
        if(!searchField.getText().equals(searchFieldText)) {
            searchFieldText = searchField.getText();

            filter.setSearchText(searchField.getText());
            synchronized (lock) {
                logCatListView.setItems(filter(filter, logList));
            }
        }
    }

    /**
     * Handles the logLevelComboBoc being pressed.
     * sets the logLevel in the filter to the logLevel from the comboBox
     * @param event
     */
    @FXML
    private void handleLogLevelComboBoxPressed(ActionEvent event) {
        logLevel = LogLevel.getLogLevel(logLevelComboBox.getSelectionModel().getSelectedIndex());

        filter.setLogLevel2(logLevel);
        logCatListView.getItems().clear();
        synchronized (lock) {
            logCatListView.setItems(filter(filter, logList));
        }
    }

    /**
     * Saves the contents of the logCat ListView to a file
     * Sets the file name as the device name followed by a time stamp
     * @param event
     */
    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        final String fileName = device.getName() + "_" + timeStamp + EXTENSION;

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
                    Log.error(ee.getMessage(), ee);
                }
                return null;
            }
        };
        task.setOnSucceeded(event1 -> resultField.setText(fileName + " saved to " + DIRECTORY));

        new Thread(task).start();
    }

    /**
     * Creates a new instance of {@link CreateFilterController} and displays it in a new window
     * @param event
     */
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

    /**
     * Removes the selected filter from the ComboBox and removes it from the list
     * @param event
     */
    @FXML
    private void handleDeleteFilterButtonClicked(ActionEvent event) {
        String fileName = filtersComboBox.getSelectionModel().getSelectedItem();
        int fileIndex = filtersComboBox.getSelectionModel().getSelectedIndex();

        File fileToDelete = new File(FILTER_DIRECTORY + "\\" + fileName + ".xml");
        if(fileToDelete.delete()) {
            filtersComboBox.getItems().remove(fileIndex);
        }
    }

    /**
     * Updates the filters ComboBox
     */
    public void updateFiltersComboBox() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        initializeComboBox();

        filtersComboBox.getSelectionModel().select(selectedFilterIndex);
    }

    /**
     * Creates a new {@link Filter} Object from the contents of the XML file selected
     * Applies this filter to the loglist and refreshes the logList with the filtered items
     * @param event
     */
    @FXML
    private void handleFiltersComboBoxPressed(ActionEvent event) {
        if(filtersComboBox.getSelectionModel().getSelectedIndex() > -1) {
            selectedFilterIndex = filtersComboBox.getSelectionModel().getSelectedIndex();

            if (selectedFilterIndex > 0) {
                filter = Filter.getFilter(filtersComboBox.getSelectionModel().getSelectedItem());
                filter.setSearchText(searchField.getText());
                filter.setLogLevel2(logLevel);
            } else {
                filter = new Filter(searchField.getText(), logLevel);
            }

            deleteFilterButton.setDisable(!(selectedFilterIndex > 0));
            editFilterButton.setDisable(!(selectedFilterIndex > 0));
            logCatListView.getItems().clear();
            synchronized (lock) {
                logCatListView.setItems(filter(filter, logList));
            }
        } 
    }

    /**
     * Edits the selected filter
     * Creates a new instance of {@link CreateFilterController} and displays it in a new window
     * @param event
     */
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

    /**
     * @return The name of the file to edit
     */
    public String getFileToEditName() {
        return fileToEdit;
    }

    /**
     * Creates an ObservableList<String> of filtered logcat output
     * @param filter filter to filter list with
     * @param list list to filter
     * @return filtered list
     */
    private ObservableList<String> filter(Filter filter, ObservableList<String> list) {
        ObservableList<String> newList = FXCollections.observableArrayList();

        if(filter.getSearchText() != null) {
            for (String s : list) {
                if (matchesFilter(filter, s))
                    newList.add(s);
            }
        }

        return newList;
    }

    /**
     * Checks if the input string is acceptable to the filter
     * @param filter filter tpo filter string with
     * @param s string to filter
     * @return boolean indicating if the input string is acceptable to the filter
     */
    private boolean matchesFilter(Filter filter, String s) {
        if (s.startsWith("-") || s.isEmpty()) {
            return false;
        }

        s = s.replaceAll(" {2,3}", " ");

        String[] split = s.split(" ");
        String pid = split[2];
        String level = split[4];
        String tag = split[5];
        String message = s.substring(s.indexOf(tag));
        message = message.substring(message.indexOf(":")+1);

        String logLevel1 = filter.getLogLevel().substring(0, 1).trim();
        String logLevel2 = filter.getLogLevel2().substring(0, 1).trim();

        if (!logLevel1.equals("N"))
            if (!level.equals(logLevel1))
                return false;

        if (!logLevel2.equals("N"))
            if (!level.equals(logLevel2))
                return false;

        return s.contains(filter.getSearchText()) &&
                s.contains(filter.getApplicationName()) &&
                (filter.getPID().isEmpty() || pid.contains(filter.getPID())) &&
                (filter.getLogTag().isEmpty() || tag.contains(filter.getLogTag())) &&
                (filter.getLogMessage().isEmpty() || message.contains(filter.getLogMessage()));
    }
}
