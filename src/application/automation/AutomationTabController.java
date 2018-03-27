package application.automation;

import application.ADBUtil;
import application.utilities.XMLUtil;
import application.utilities.Utilities;
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

public class AutomationTabController implements Initializable {

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
    private ComboBox<String> runTypeComboBox;

    @FXML
    private CheckBox stopOnFailureCheckBox;

    private CreateBatchTabController createBatchTabController;

    protected Task<Void> runCommandsTask;
    protected Thread runCommandsThread;

    protected ObservableList<String> filesList;
    private ObservableList<String> commandsList;

    protected File directory = null;

    protected AtomicBoolean pauseFlag = new AtomicBoolean(false);
    protected AtomicBoolean wasPaused = new AtomicBoolean(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        createBatchTabController = new CreateBatchTabController();

        runTypeComboBox.getSelectionModel().select(0);
        stopButton.setDisable(true);
        deleteButton.setDisable(true);
        editButton.setDisable(true);
        playButton.setDisable(true);

        filesList = FXCollections.observableArrayList();
        directory = new File(DIRECTORY);

        updateCommandsList();

        try {
            if (!filesListView.getSelectionModel().getSelectedItem().isEmpty())
                refreshCommandsList();
        } catch (Exception e) {}

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

                    Utilities.setImage("/resources/pause.png", "Pause batch commands", playButton);
                    wasPaused.set(true);
                }
            }
        }

        if(!wasPaused.get() && !pauseFlag.get()) {
            Utilities.setImage("/resources/pause.png", "Pause batch commands", playButton);
            System.out.println("Starting new batch automation");
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

                        String result = ADBUtil.consoleCommand(formatCommand(command).split(" "));
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
                stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsTask.setOnFailed(event1 -> {
                Utilities.setImage("/resources/play.png","Run batch commands", playButton);
                System.out.println("runCommandsTask failed, Exception: " + runCommandsTask.getException());
                stopButton.setDisable(true);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsThread = new Thread(runCommandsTask);
            runCommandsThread.start();
        }
    }

    @FXML
    protected void handleStopButtonClicked(ActionEvent event) {
        stopButton.setDisable(true);
        runCommandsThread.interrupt();
        runCommandsTask.cancel();
        wasPaused.set(false);
        pauseFlag.set(false);
        Utilities.setImage("/resources/play.png","Run batch commands", playButton);
    }

    @FXML
    protected void handleEditButtonClicked(ActionEvent event) {
        String fileName = filesListView.getSelectionModel().getSelectedItem();
        File editFile = new File(directory.getAbsolutePath() + "\\" + fileName + ".xml");

        try {
            createBatchTabController.newWindow(this, editFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    protected void handleNewButtonClicked(ActionEvent event) {
        try {
            createBatchTabController.newWindow(this, null);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    protected void initializeButtons() {
        Utilities.setImage("/resources/play.png","Run batch commands", playButton);
        Utilities.setImage("/resources/stop.png", "Stop batch commands", stopButton);

        Utilities.setImage("/resources/new.png", "Create new batch",newButton);
        Utilities.setImage("/resources/edit.png", "Edit selected batch",editButton);
        Utilities.setImage("/resources/delete.png","Delete selected batch", deleteButton);
    }

    public void refreshCommandsList() {
        XMLUtil xmlUtil = new XMLUtil();

        String commandName = filesListView.getSelectionModel().getSelectedItem();
        System.out.println("Command name: " + commandName);
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

    public void updateCommandsList() {
        try {
            filesListView.getItems().clear();
            filesList.clear();
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                filesList.add(file.getName().replace(".xml", ""));
            }
        } catch (NullPointerException npe) {
            //npe.printStackTrace();
        }

        filesListView.setItems(filesList);
    }

    private String formatCommand(String command) {
        if(command.startsWith("shell input text")) {
            String tempCommand = command.substring(17);
            //System.out.println("TempCommand: " + tempCommand);

            StringBuilder temp2 = new StringBuilder();
            for(char ch : tempCommand.toCharArray()) {
                String temp = ch+"";
                if(!Character.isAlphabetic(ch) && ch != ' ')
                    temp = "\\" + temp;

                temp2.append(temp);
            }

            tempCommand = temp2.toString();
            //tempCommand = tempCommand.replaceAll("[^a-zA-Z0-9 ]", "\\[^a-zA-Z0-9]");
            tempCommand = tempCommand.replace(" ", "%s");

            //System.out.println("TempCommand now: " + tempCommand);
            tempCommand  = "\"" + tempCommand + "\"";

            command = "shell input text " + tempCommand;
        }

        //System.out.print("Command: " + command);
        return command;
    }

    private boolean isError(String input) {
        return (input.startsWith("Failure") || input.startsWith("Error") || input.startsWith("error") || input.startsWith("** No activities found"));
    }
}
