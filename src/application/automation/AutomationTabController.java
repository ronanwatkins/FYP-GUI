package application.automation;

import application.utilities.ADBUtil;
import application.applications.ApplicationTabController;
import application.logcat.LogCatTabController;
import application.monitor.MonitorTabController;
import application.utilities.ApplicationUtils;
import application.utilities.XMLUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutomationTabController extends ApplicationTabController implements Initializable, ApplicationUtils {

    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\automation";
    protected final String EXTENSION = ".xml";

    @FXML
    protected ListView<String> filesListView;
    @FXML
    private ListView<String> allCommandsListView;
    @FXML
    private ListView<String> runningCommandsListView;

    @FXML
    protected Button editButton;
    @FXML
    protected Button newButton;
    @FXML
    protected Button deleteButton;
    @FXML
    protected Button playButton;
    @FXML
    protected Button stopButton;
    @FXML
    private Button showMonitorButton;

    @FXML
    protected ComboBox<String> runTypeComboBox;

    @FXML
    private CheckBox stopOnFailureCheckBox;

    private CreateBatchController createBatchController;
    private LogCatTabController logCatTabController;

    MonitorTabController monitorTabController = null;

    protected Task<Void> runCommandsTask;
    protected Thread runCommandsThread;

    protected ObservableList<String> filesList;
    private ObservableList<String> commandsList;

    protected File directory = null;

    protected AtomicBoolean pauseFlag = new AtomicBoolean(false);
    protected AtomicBoolean wasPaused = new AtomicBoolean(false);
    
    private final int INTERVAL = 2000;

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
        createBatchController = new CreateBatchController();

        runTypeComboBox.getSelectionModel().select(0);

        filesList = FXCollections.observableArrayList();
        directory = new File(DIRECTORY);

        updateCommandsList();

        try {
            if (!filesListView.getSelectionModel().getSelectedItem().isEmpty())
                refreshCommandsList();
        } catch (Exception ignored) {}

        filesListView.setOnMouseClicked(event -> {
            try {
                if (!filesListView.getSelectionModel().getSelectedItem().isEmpty()) {
                    deleteButton.setDisable(false);
                    editButton.setDisable(false);
                    playButton.setDisable(false);
                    stopButton.fire();

                    refreshCommandsList();
                }
            } catch (NullPointerException ignored) {}
        });
    }

    /**
     * Deletes the selected file and removes it from the ListView
     * @param event
     */
    @FXML
    protected void handleDeleteButtonClicked(ActionEvent event) {
        String fileName = filesListView.getSelectionModel().getSelectedItem();
        int fileIndex = filesListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(DIRECTORY + "\\" + fileName + EXTENSION);
        if(fileToDelete.delete()) {
            filesList.remove(fileIndex);
            refreshCommandsList();
        }
    }

    /**
     * Runs through the ListView of batch commands
     * Sends each command to the connected device over ADB at an interval of {@link #INTERVAL}
     * Pauses if it is already running
     * @param event
     */
    @FXML
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

                    setImage("/resources/pause.png", "Pause batch commands", playButton);
                    wasPaused.set(true);
                }
            }
        }

        if(!wasPaused.get() && !pauseFlag.get()) {
            setImage("/resources/pause.png", "Pause batch commands", playButton);
            Log.info("Starting new batch automation");
            runningCommandsListView.getItems().clear();
            stopButton.setDisable(false);

            commandsList = allCommandsListView.getItems();

            int startIndex;
            if(runTypeComboBox.getSelectionModel().isSelected(1) || runTypeComboBox.getSelectionModel().isSelected(2))
                startIndex = allCommandsListView.getSelectionModel().getSelectedIndex();
            else
                startIndex = 0;

            int endIndex;
            if(runTypeComboBox.getSelectionModel().isSelected(2))
                endIndex = startIndex+1;
            else
                endIndex = allCommandsListView.getItems().size();

            runCommandsTask = new Task<Void>() {
                @Override
                protected Void call() {
                    int index = startIndex;
                    for (String command : commandsList.subList(startIndex, endIndex)) {
                        Log.info("Running command: " + command);

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

                        String result = ADBUtil.consoleCommand(formatCommand(command));
                        if(stopOnFailureCheckBox.isSelected() && (isError(result)))
                            Platform.runLater(() -> stopButton.fire());

                        if (result.isEmpty())
                            result = "Command completed successfully";

                        final String newResult = result;
                        final int newIndex = index++;

                        Platform.runLater(() -> {
                            allCommandsListView.getSelectionModel().select(newIndex);

                            if (allCommandsListView.getSelectionModel().getSelectedIndex() > 10) {
                                allCommandsListView.scrollTo(allCommandsListView.getSelectionModel().getSelectedIndex());
                            }

                            runningCommandsListView.getItems().add(newResult);
                            int selectedIndex = allCommandsListView.getSelectionModel().getSelectedIndex() - startIndex;
                            runningCommandsListView.getSelectionModel().select(selectedIndex);
                            runningCommandsListView.scrollTo(selectedIndex);
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
                stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsTask.setOnFailed(event1 -> {
                setImage("/resources/play.png","Run batch commands", playButton);
                Log.info("runCommandsTask failed, Exception: " + runCommandsTask.getException());
                stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsThread = new Thread(runCommandsTask);
            runCommandsThread.start();
        }
    }

    /**
     * Stops sending batch commands to the connected device
     * @param event
     */
    @FXML
    protected void handleStopButtonClicked(ActionEvent event) {
        stopButton.setDisable(true);
        runCommandsThread.interrupt();
        runCommandsTask.cancel();
        wasPaused.set(false);
        pauseFlag.set(false);
        setImage("/resources/play.png","Run batch commands", playButton);
    }

    /**
     * Edits the selected batch file.
     * Shows the create / edit batch GUI with contents of the selected file
     * @param event
     */
    @FXML
    protected void handleEditButtonClicked(ActionEvent event) {
        String fileName = filesListView.getSelectionModel().getSelectedItem();
        File editFile = new File(directory.getAbsolutePath() + "\\" + fileName + ".xml");

        try {
            createBatchController.newWindow(this, editFile);
        } catch (IOException ioe) {
           Log.error(ioe.getMessage(), ioe);
        }
    }

    /**
     * Creates an instance of the {@link MonitorTabController} and displays it
     * @param event
     */
    @FXML
    private void handleShowMonitorButtonClicked(ActionEvent event) {
        if (monitorTabController == null) {
            monitorTabController = new MonitorTabController();
            try {
                monitorTabController = (MonitorTabController) monitorTabController.newWindow(this, null);
                monitorTabController.play();
            } catch (IOException ioe) {
                Log.error(ioe.getMessage(), ioe);
            }
        }
    }

    /**
     * Sets the instance of {@link MonitorTabController}
     * @param monitorTabController
     */
    public void setMonitorTabController(MonitorTabController monitorTabController) {
        this.monitorTabController = monitorTabController;
    }

    /**
     * Creates an instance of the {@link LogCatTabController} and displays it
     * @param event
     */
    public void handleShowLogCatButtonClicked(ActionEvent event) {
        if (logCatTabController == null) {
            logCatTabController = new LogCatTabController();
            try {
                logCatTabController = (LogCatTabController) logCatTabController.newWindow(this, null);
                logCatTabController.getLogs(true);
            } catch (IOException ioe) {
                Log.error(ioe.getMessage(), ioe);
            }
        }
    }

    /**
     * Sets the instance of {@link LogCatTabController}
     * @param logCatTabController
     */
    public void setLogCatTabController(LogCatTabController logCatTabController) {
        this.logCatTabController = logCatTabController;
    }

    /**
     * Shows the createBatchController controller to create a new Batch
     * @param event
     */
    @FXML
    protected void handleNewButtonClicked(ActionEvent event) {
        try {
            createBatchController.newWindow(this, null);
        } catch (IOException ioe) {
            Log.error(ioe.getMessage(), ioe);
        }
    }

    /**
     * Refreshes the ListView of batch commands
     */
    public void refreshCommandsList() {
        XMLUtil xmlUtil = new XMLUtil(false);

        String commandName = filesListView.getSelectionModel().getSelectedItem();
        Log.info("Command name: " + commandName);
        if(commandName != null) {
            ObservableList<String> batchCommands = xmlUtil.openBatchCommands(new File(DIRECTORY + "\\" + commandName + ".xml"));
            allCommandsListView.setItems(batchCommands);
        } else {
            allCommandsListView.setItems(null);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            playButton.setDisable(true);
        }
    }

    /**
     * Updates the ListView of batch files
     */
    public void updateCommandsList() {
        try {
            filesListView.getItems().clear();
            filesList.clear();
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                filesList.add(file.getName().replace(".xml", ""));
            }
        } catch (NullPointerException npe) {
            Log.error(npe.getMessage(), npe);
        }

        filesListView.setItems(filesList);
    }

    /**
     * Returns a formatted string readable by the emulator
     * @param command
     * @return Formatted string readable by the emulator
     */
    private String formatCommand(String command) {
        if(command.startsWith("shell input text")) {
            String tempCommand = command.substring(17);

            StringBuilder temp2 = new StringBuilder();
            for(char ch : tempCommand.toCharArray()) {
                String temp = ch+"";
                if(!Character.isAlphabetic(ch) && ch != ' ')
                    temp = "\\" + temp;

                temp2.append(temp);
            }

            tempCommand = temp2.toString();
            tempCommand = tempCommand.replace(" ", "%s");

            tempCommand  = "\"" + tempCommand + "\"";

            command = "shell input text " + tempCommand;
        }

        return command;
    }

    private boolean isError(String input) {
        return (input.startsWith("Failure") || input.startsWith("Error") || input.startsWith("error") || input.startsWith("** No activities found"));
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
        stopButton.setDisable(true);
        deleteButton.setDisable(true);
        editButton.setDisable(true);
        playButton.setDisable(true);
        
        setImage("/resources/play.png","Run batch commands", playButton);
        setImage("/resources/stop.png", "Stop batch commands", stopButton);
        setImage("/resources/pop_out.png", null, showMonitorButton);
        setImage("/resources/pop_out.png", null, showLogCatButton);

        setImage("/resources/new.png", "Create new batch",newButton);
        setImage("/resources/edit.png", "Edit selected batch",editButton);
        setImage("/resources/delete.png","Delete selected batch", deleteButton);
    }
}
