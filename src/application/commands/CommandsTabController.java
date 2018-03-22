package application.commands;

import application.ADBUtil;
import application.XMLUtil;
import application.utilities.Utilities;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandsTabController implements Initializable {

    private final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\commands";

    @FXML
    private TextField commandField;

    @FXML
    private ListView<String> possibleCommandsListView;
    @FXML
    private ListView<String> commandsListView;
    @FXML
    private ListView<Integer> indexListView;
    @FXML
    private ListView<String> selectListView;
    @FXML
    private ListView<String> allCommandsListView;
    @FXML
    private ListView<String> runningCommandsListView;

    @FXML
    private Button addCommandButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button moveUpButton;
    @FXML
    private Button moveDownButton;
    @FXML
    private Button saveButton;

    @FXML
    private Button editButton;
    @FXML
    private Button deleteCommandsButton;
    @FXML
    private Button runCommandsButton;
    @FXML
    private Button stopCommandsButton;
    @FXML
    private Button enterTextButton;

    @FXML
    private ComboBox<String> runTypeComboBox;

    @FXML
    private CheckBox stopOnFailureCheckBox;

    @FXML
    private ToggleButton inputsToggleButton;
    @FXML
    private ToggleButton applicationsToggleButton;

    @FXML
    private Label actionLabel;

    @FXML
    private ComboBox<String> actionComboBox;

    @FXML
    private Tab runBatchTab;
    @FXML
    private Tab createBatchTab;

    @FXML
    private TabPane tabPane;

    @FXML
    private ChoiceBox<Integer> indexBox;

    private HashMap<String, String> inputCommandsMap;
    private HashMap<String, String> applicationCommandsMap;
    private HashMap<String, String> actionMap;

    private int index = 0;
    private int selectedIndex = 0;

    private final String keyEvent = "shell input keyevent ";

    private File editFile = null;
    private File directory = null;

    private ObservableList<String> inputCommands;
    private ObservableList<String> applicationCommands;
    private ObservableList<String> commandFilesList;
    private ObservableList<Integer> indexList;

    private Task<Void> runCommandsTask;
    private Thread runCommandsThread;

    private AtomicBoolean pauseFlag = new AtomicBoolean(false);
    private AtomicBoolean wasPaused = new AtomicBoolean(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        runBatchTab();

        createBatchTab.setOnSelectionChanged(event -> {
            if(createBatchTab.isSelected()) {
                System.out.println("Create Batch Tab Selected");

                createBatchTab(editFile);
            }
        });

        runBatchTab.setOnSelectionChanged(event -> {
            if(runBatchTab.isSelected()) {
                System.out.println("Run Batch Tab Selected");
                runBatchTab();
            }
        });
    }

    private void initializeButtons() {
        ToggleGroup toggleGroup = new ToggleGroup();
        inputsToggleButton.setToggleGroup(toggleGroup);
        applicationsToggleButton.setToggleGroup(toggleGroup);
        inputsToggleButton.setSelected(true);

        Utilities.setImage("/resources/play.png", runCommandsButton);
        Utilities.setImage("/resources/stop.png", stopCommandsButton);

        Utilities.setImage("/resources/edit.png", editButton);
        Utilities.setImage("/resources/delete.png", deleteCommandsButton);

        Utilities.setImage("/resources/right.png", addCommandButton);
        Utilities.setImage("/resources/up.png", moveUpButton);
        Utilities.setImage("/resources/down.png", moveDownButton);
        Utilities.setImage("/resources/delete.png", deleteButton);
    }

    private void createBatchTab(File file) {
        stopCommandsButton.fire();
        initInputCommandsMap();
        initApplicationCommandsMap();

        if(file != null) {
            saveButton.setDisable(false);
            XMLUtil xmlUtil = new XMLUtil();

            ObservableList<String> commands = xmlUtil.openBatchCommands(file);
            commandsListView.setItems(commands);

            indexList = FXCollections.observableArrayList();
            index = commands.size();
            for (int i = 0; i <= index; i++) {
                indexList.add(i);
            }
            indexBox.setItems(indexList);
            indexBox.setValue(index);

            indexListView.getItems().clear();
            for (int i = 0; i < indexBox.getValue(); i++) {
                indexListView.getItems().add(i);
            }
        } else {
            saveButton.setDisable(true);

            index = 0;
            indexList = FXCollections.observableArrayList(
                    index
            );

            indexListView.getItems().clear();
            commandsListView.getItems().clear();
        }

        inputCommands = FXCollections.observableArrayList(
                inputCommandsMap.keySet()
        );

        actionComboBox.setVisible(false);
        actionLabel.setVisible(false);
        inputsToggleButton.setSelected(true);

        commandField.setText("");

        deleteButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);

        indexBox.setItems(indexList);
        indexBox.setValue(index);

        possibleCommandsListView.setItems(inputCommands);

        if(inputsToggleButton.isSelected()) {
            possibleCommandsListView.setOnMouseClicked(event -> commandField.setText(keyEvent + inputCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
        } else {
            possibleCommandsListView.setOnMouseClicked(event -> commandField.setText(keyEvent + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
        }

        indexBox.setItems(indexList);

        commandsListView.setOnMouseClicked(event -> {
            try {
                if (!commandsListView.getSelectionModel().getSelectedItem().isEmpty()) {
                    deleteButton.setDisable(false);
                    moveDownButton.setDisable(false);
                    moveUpButton.setDisable(false);
                }
            } catch (NullPointerException npe) {
                //npe.printStackTrace();
            }
        });
    }

    private void runBatchTab() {
        editFile = null;
        runTypeComboBox.getSelectionModel().select(0);
        stopCommandsButton.setDisable(true);
        deleteCommandsButton.setDisable(true);
        editButton.setDisable(true);
        runCommandsButton.setDisable(true);

        commandFilesList = FXCollections.observableArrayList();

        directory = new File(DIRECTORY);

        try {
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                commandFilesList.add(file.getName().replace(".xml", ""));
            }
        } catch (NullPointerException npe) {
            //npe.printStackTrace();
        }

        selectListView.setItems(commandFilesList);

        try {
            if (!selectListView.getSelectionModel().getSelectedItem().isEmpty())
                refreshCommandsList();
        } catch (Exception e) {}

        selectListView.setOnMouseClicked(event -> {
            try {
                if (!selectListView.getSelectionModel().getSelectedItem().isEmpty()) {
                    deleteCommandsButton.setDisable(false);
                    editButton.setDisable(false);
                    runCommandsButton.setDisable(false);
                    stopCommandsButton.fire();

                    refreshCommandsList();
                }
            } catch (NullPointerException npe) {}
        });
    }

    //Run Tab
    @FXML
    private void handleInputsButtonPressed(ActionEvent event) {
        inputsToggleButton.setSelected(true);
        actionComboBox.setVisible(false);
        actionLabel.setVisible(false);

        possibleCommandsListView.setItems(inputCommands);
        possibleCommandsListView.setOnMouseClicked(mouseEvent -> commandField.setText(keyEvent + inputCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
    }

    @FXML
    private void handleApplicationsButtonPressed(ActionEvent event) {
        applicationsToggleButton.setSelected(true);
        actionComboBox.setVisible(true);
        actionLabel.setVisible(true);

        initActionMap();


        actionComboBox.setItems(FXCollections.observableArrayList(
                actionMap.keySet()
        ));
        actionComboBox.getSelectionModel().select(5);

        applicationCommands = FXCollections.observableArrayList(
                applicationCommandsMap.keySet()
        );
        possibleCommandsListView.setItems(applicationCommands);
        possibleCommandsListView.getSelectionModel().select(selectedIndex);

        possibleCommandsListView.setOnMouseClicked(mouseEvent -> {
            System.out.println("here 1");
            if(possibleCommandsListView.getSelectionModel().getSelectedItem() != null) {
                commandField.setText(actionMap.get(actionComboBox.getValue()) + " "
                        + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
                if (actionComboBox.getSelectionModel().isSelected(5))
                    commandField.setText(commandField.getText() + " 1");

                selectedIndex = possibleCommandsListView.getSelectionModel().getSelectedIndex();
            }
        });

//        actionComboBox.setOnAction(mouseEvent -> {
//            System.out.println("here 2");
//            if(possibleCommandsListView.getSelectionModel().getSelectedItem() != null) {
//                commandField.setText(actionMap.get(actionComboBox.getValue()) + " "
//                        + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
//                if (actionComboBox.getSelectionModel().isSelected(5))
//                    commandField.setText(commandField.getText() + " 1");
//            }
//        });

        //actionComboBox.setOn
    }

    @FXML
    private void handleActionComboBoxClicked(ActionEvent event) {
        System.out.println("here 2");
        //if(possibleCommandsListView.getSelectionModel().getSelectedItem() != null || commandField.getText().contains(".")) {
        if (actionMap.get(actionComboBox.getValue()) != null && possibleCommandsListView.getSelectionModel().getSelectedItem() !=  null) {
            commandField.setText(actionMap.get(actionComboBox.getValue()) + " "
                    + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
            if (actionComboBox.getSelectionModel().isSelected(5))
                commandField.setText(commandField.getText() + " 1");
        }
    }

    @FXML
    private void handleEditButtonClicked(ActionEvent event) {
        String fileName = selectListView.getSelectionModel().getSelectedItem();
        editFile = new File(directory.getAbsolutePath() + "\\" + fileName + ".xml");
        createBatchTab(editFile);
        tabPane.getSelectionModel().select(createBatchTab);
    }

    @FXML
    private void handleDeleteCommandsButtonClicked(ActionEvent event) {
        String fileName = selectListView.getSelectionModel().getSelectedItem();
        int fileIndex = selectListView.getSelectionModel().getSelectedIndex();
        File fileToDelete = new File(directory.getAbsolutePath() + "\\" + fileName + ".xml");
        if(fileToDelete.delete()) {
            System.out.println("File deleted");
            commandFilesList.remove(fileIndex);

            refreshCommandsList();
        }

    }

    @FXML
    private void handleRunCommandsButtonClicked(ActionEvent event) {
        if(runCommandsTask != null) {
            if(runCommandsTask.isRunning()) {
                if (!pauseFlag.get()) {
                    synchronized (pauseFlag) {
                        pauseFlag.set(true);
                    }

                    Utilities.setImage("/resources/play.png", runCommandsButton);
                } else {
                    synchronized (pauseFlag) {
                        pauseFlag.set(false);
                        pauseFlag.notify();
                    }

                    Utilities.setImage("/resources/pause.png", runCommandsButton);
                    wasPaused.set(true);
                }
            }
        }

        if(!wasPaused.get() && !pauseFlag.get()) {
            Utilities.setImage("/resources/pause.png", runCommandsButton);
            System.out.println("Starting new batch commands");
            runningCommandsListView.getItems().clear();
            stopCommandsButton.setDisable(false);

            ObservableList<String> commandsList = allCommandsListView.getItems();

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

                        System.out.println("Running: " + formatCommand(command));
                        String result = ADBUtil.consoleCommand(formatCommand(command).split(" "));
                        if(stopOnFailureCheckBox.isSelected() && (isError(result)))
                            Platform.runLater(() -> stopCommandsButton.fire());

                        if (result.isEmpty())
                            result = "Command completed successfully";

                        final String newResult = result;
                        final int newIndex = index++;

                        Platform.runLater(() -> {
                            allCommandsListView.getSelectionModel().select(newIndex);

                            if (allCommandsListView.getSelectionModel().getSelectedIndex() > 10) {
                                //allCommandsListView.scrollTo(newIndex);
                                allCommandsListView.scrollTo(allCommandsListView.getSelectionModel().getSelectedIndex());
                            }

                            runningCommandsListView.getItems().add(newResult);
                            int selectedIndex = allCommandsListView.getSelectionModel().getSelectedIndex();
                            runningCommandsListView.getSelectionModel().select(selectedIndex);
                            //System.out.println("runningCommandsListView index: " + allCommandsListView.getSelectionModel().getSelectedIndex());
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
                Utilities.setImage("/resources/play.png", runCommandsButton);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsTask.setOnFailed(event1 -> {
                Utilities.setImage("/resources/play.png", runCommandsButton);
                wasPaused.set(false);
                pauseFlag.set(false);
            });

            runCommandsThread = new Thread(runCommandsTask);
            runCommandsThread.start();
        }
    }

    @FXML
    private void handleStopCommandsButtonClicked(ActionEvent event) {
        stopCommandsButton.setDisable(true);
        runCommandsThread.interrupt();
        runCommandsTask.cancel();
        wasPaused.set(false);
        pauseFlag.set(false);
        Utilities.setImage("/resources/play.png", runCommandsButton);
    }

    //Create Tab
    @FXML
    private void handleGetCursorLocationClicked(ActionEvent event) {
        try {
            GetTouchPositionController.showScreen(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRecordInputsClicked(ActionEvent event) {
        try {
            RecordInputsController.showScreen(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddCommandButtonClicked(ActionEvent event) {
        if(!commandField.getText().isEmpty()) {
            saveButton.setDisable(false);
            commandsListView.getItems().add(indexBox.getValue(), commandField.getText());
            indexList.add(index++);
            indexBox.setItems(indexList);
            indexBox.setValue(index);
            indexListView.getItems().clear();
            for (int i = 0; i < indexBox.getValue(); i++) {
                indexListView.getItems().add(i);
            }
        }
    }

    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        int commandsListViewIndex = commandsListView.getSelectionModel().getSelectedIndex();
        if (commandsListViewIndex > -1) {
            try {
                commandsListView.getItems().remove(commandsListViewIndex);
                indexList.remove(index--);
                indexBox.setItems(indexList);
                indexBox.setValue(index);
                indexListView.getItems().clear();
            } catch (Exception ee) {
            }
            for (int i = 0; i < indexBox.getValue(); i++) {
                indexListView.getItems().add(i);
            }
        }
    }

    @FXML
    private void handleMoveUpButtonClicked(ActionEvent event) {
        int selectedItemIndex = commandsListView.getSelectionModel().getSelectedIndex();
        if(selectedItemIndex > 0) {
            String selectedItemValue = commandsListView.getSelectionModel().getSelectedItem();
            commandsListView.getItems().remove(selectedItemIndex);
            commandsListView.getItems().add(selectedItemIndex - 1, selectedItemValue);

            commandsListView.getSelectionModel().select(selectedItemIndex - 1);
        }
    }

    @FXML
    private void handleMoveDownButtonClicked(ActionEvent event) {
        int selectedItemIndex = commandsListView.getSelectionModel().getSelectedIndex();
        try {
            if (!commandsListView.getItems().get(selectedItemIndex + 1).isEmpty()) {
                String selectedItemValue = commandsListView.getSelectionModel().getSelectedItem();
                commandsListView.getItems().remove(selectedItemIndex);
                commandsListView.getItems().add(selectedItemIndex + 1, selectedItemValue);

                commandsListView.getSelectionModel().select(selectedItemIndex + 1);
            }
        } catch (IndexOutOfBoundsException e) {}
    }

    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        XMLUtil xmlUtil = new XMLUtil();

        if(editFile != null) {
            xmlUtil.saveBatchCommands(commandsListView.getItems(), editFile);
            editFile = null;
            tabPane.getSelectionModel().select(runBatchTab);
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save Batch Commands");
            dialog.setHeaderText("Enter Batch Name");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println(result.get());

                xmlUtil.saveBatchCommands(commandsListView.getItems(), new File(DIRECTORY + "\\" + result.get() + ".xml"));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                } finally {
                    tabPane.getSelectionModel().select(runBatchTab);
                }
            }
        }
    }

    @FXML
    private void handleEnterTextButtonClicked(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Text");
        dialog.setHeaderText("Type Text To Enter");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> commandField.setText("shell input text " + s));
    }

    //Utilities
    public void setCommandText(String text) {
        commandField.setText(text);
    }

    public TextField getCommandField() {
        return commandField;
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
        return (input.startsWith("Failure") || input.startsWith("Error") || input.startsWith("** No activities found"));
    }

    private void refreshCommandsList() {
        XMLUtil xmlUtil = new XMLUtil();

        String commandName = selectListView.getSelectionModel().getSelectedItem();
        System.out.println("Command name: " + commandName);
        if(commandName != null) {
            ObservableList<String> batchCommands = xmlUtil.openBatchCommands(new File(DIRECTORY + "\\" + commandName + ".xml"));
            allCommandsListView.setItems(batchCommands);
        } else {
            allCommandsListView.setItems(null);
            editButton.setDisable(true);
            deleteCommandsButton.setDisable(true);
            runCommandsButton.setDisable(true);
        }
    }

    private void initActionMap() {
        actionMap = new HashMap<>();
        actionMap.put("Disable App", "shell pm disable");
        actionMap.put("Enable App", "shell pm enable");
        actionMap.put("Clear App Data", "shell pm clear");
        actionMap.put("Uninstall", "shell pm uninstall");
        actionMap.put("Close", "shell am force-stop");
        actionMap.put("Open", "shell monkey -p");
    }

    private void initApplicationCommandsMap() {
        applicationCommandsMap = new HashMap<>();

        ArrayList<String> applications = ADBUtil.listApplications();
        for(String application : applications) {
            applicationCommandsMap.put(application, application);
        }
    }

    private void initInputCommandsMap() {
        inputCommandsMap = new HashMap<>();
        inputCommandsMap.put("Back", "KEYCODE_BACK");
        inputCommandsMap.put("Home", "KEYCODE_HOME");
        inputCommandsMap.put("List Open Apps", "KEYCODE_APP_SWITCH");
        inputCommandsMap.put("Volume Up", "KEYCODE_VOLUME_UP");
        inputCommandsMap.put("Volume Down", "KEYCODE_VOLUME_DOWN");
        inputCommandsMap.put("Power", "KEYCODE_POWER");
        inputCommandsMap.put("Enter", "KEYCODE_ENTER");
        inputCommandsMap.put("Menu", "KEYCODE_MENU");
        inputCommandsMap.put("Call", "KEYCODE_CALL");
        inputCommandsMap.put("End Call", "KEYCODE_ENDCALL");
        inputCommandsMap.put("Up", "KEYCODE_DPAD_UP");
        inputCommandsMap.put("Down", "KEYCODE_DPAD_DOWN");
        inputCommandsMap.put("Left", "KEYCODE_DPAD_LEFT");
        inputCommandsMap.put("Right", "KEYCODE_DPAD_RIGHT");
        inputCommandsMap.put("Centre", "KEYCODE_DPAD_CENTRE");
        inputCommandsMap.put("Camera", "KEYCODE_CAMERA");
        inputCommandsMap.put("Tab", "KEYCODE_TAB");
        inputCommandsMap.put("Space", "KEYCODE_SPACE");
        inputCommandsMap.put("Change Keyboard", "KEYCODE_SYM");
        inputCommandsMap.put("Browser", "KEYCODE_EXPLORER");
        inputCommandsMap.put("Gmail", "KEYCODE_ENVELOPE");
        inputCommandsMap.put("Delete", "KEYCODE_DEL");
        inputCommandsMap.put("Search", "KEYCODE_SEARCH");
    }
}
