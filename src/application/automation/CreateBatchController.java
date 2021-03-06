package application.automation;

import application.utilities.ADBUtil;
import application.device.Intent;
import application.utilities.ApplicationUtils;
import application.utilities.XMLUtil;
import application.automation.extras.GetTouchPositionController;
import application.utilities.Showable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CreateBatchController implements Initializable, Showable<AutomationTabController>, ApplicationUtils {

    private final String FILES_DIRECTORY = System.getProperty("user.dir") + "\\misc";
    private final String EXTENSION = ".xml";
    private static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\automation";

    @FXML
    private TextField commandField;
    @FXML
    private TextField filesTextField1;
    @FXML
    private TextField filesTextField2;
    @FXML
    private TextField actionTextField;
    @FXML
    private TextField dataTextField;
    @FXML
    private TextField mimeTypeTextField;
    @FXML
    private TextField categoryTextField;
    @FXML
    private TextField componentTextField;
    @FXML
    private TextField flagsTextField;

    @FXML
    private ListView<String> possibleCommandsListView;
    @FXML
    private ListView<String> commandsListView;
    @FXML
    private ListView<Integer> indexListView;

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
    private Button filesFinder1;
    @FXML
    private Button filesFinder2;
    @FXML
    private Button enterButton;

    @FXML
    private ToggleButton inputsToggleButton;
    @FXML
    private ToggleButton applicationsToggleButton;
    @FXML
    private ToggleButton filesToggleButton;
    @FXML
    private ToggleButton actionsToggleButton;

    @FXML
    private Label actionLabel;
    @FXML
    private Label filesLabel1;
    @FXML
    private Label filesLabel2;

    @FXML
    private Hyperlink actionLabel1;
    @FXML
    private Hyperlink dataLabel;
    @FXML
    private Hyperlink mimeTypeLabel;
    @FXML
    private Hyperlink categoryLabel;
    @FXML
    private Hyperlink componentLabel;
    @FXML
    private Hyperlink flagsLabel;
    @FXML
    private Hyperlink helpLabel;

    @FXML
    private ComboBox<String> applicationActionComboBox;
    @FXML
    private ComboBox<String> selectionComboBox;
    @FXML
    private ComboBox<String> actionComboBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> mimeTypeComboBox;
    @FXML
    private ComboBox<String> componentComboBox;

    @FXML
    private ChoiceBox<Integer> indexBox;

    private static AutomationTabController controller;

    private int index = 1;
    private int selectedIndex = 0;
    private int intentType;

    private static File editFile;

    private final String keyEvent = "shell input keyevent ";

    private HashMap<String, String> inputCommandsMap;
    private HashMap<String, String> applicationCommandsMap;
    private HashMap<String, String> actionMap;

    private ObservableList<String> inputCommands;
    private ObservableList<Integer> indexList;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     * @param location
     * @param resources
     * */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showFilesSection(false);
        showActionsSection(false);
        initApplicationCommandsMap();
        initializeButtons();
        initInputCommandsMap();
        filesFinder1.setVisible(false);
        filesFinder2.setVisible(false);

        commandsListView.setEditable(true);
        commandsListView.setCellFactory(TextFieldListCell.forListView());

        if(editFile != null) {
            saveButton.setDisable(false);
            XMLUtil xmlUtil = new XMLUtil(false);

            ObservableList<String> commands = xmlUtil.openBatchCommands(editFile);
            commandsListView.setItems(commands);

            indexList = FXCollections.observableArrayList();
            index = commands.size();
            for (int i = 0; i <= index; i++)
                indexList.add(i);
            indexBox.setItems(indexList);
            indexBox.setValue(index);

            indexListView.getItems().clear();
            for (int i = 0; i < indexBox.getValue(); i++)
                indexListView.getItems().add(i);
        } else {
            saveButton.setDisable(true);

            index = 0;
            indexList = FXCollections.observableArrayList(index);

            indexListView.getItems().clear();
            commandsListView.getItems().clear();
        }

        inputCommands = FXCollections.observableArrayList(inputCommandsMap.keySet());

        applicationActionComboBox.setVisible(false);
        actionLabel.setVisible(false);
        inputsToggleButton.setSelected(true);

        commandField.setText("");

        deleteButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);

        indexBox.setItems(indexList);
        indexBox.setValue(index);

        possibleCommandsListView.setItems(inputCommands);

        if(inputsToggleButton.isSelected())
            possibleCommandsListView.setOnMouseClicked(event -> commandField.setText(keyEvent + inputCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
        else
            possibleCommandsListView.setOnMouseClicked(event -> commandField.setText(keyEvent + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));

        commandsListView.setOnMouseClicked(event -> {
            try {
                if (!commandsListView.getSelectionModel().getSelectedItem().isEmpty()) {
                    deleteButton.setDisable(false);
                    moveDownButton.setDisable(false);
                    moveUpButton.setDisable(false);
                }
            } catch (NullPointerException npe) {
                Log.error(npe.getMessage(), npe);
            }
        });

        commandsListView.setOnEditCommit(t -> commandsListView.getItems().set(t.getIndex(), t.getNewValue()));
    }

    @Override
    public Initializable newWindow(AutomationTabController commandsTabController, Object object) throws IOException {
        controller = commandsTabController;
        editFile = (File) object;

        FXMLLoader fxmlLoader = new FXMLLoader(commandsTabController.getClass().getResource("/application/automation/CreateBatchView.fxml"));
        Parent root = fxmlLoader.load();
        CreateBatchController createBatchController = fxmlLoader.getController();
        root.getStylesheets().add("/application/main/global.css");

        Stage stage = new Stage();

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        String title = object == null ? "Create Batch Commands" : "Editing \"" + editFile.getName().replace(".xml", "") + "\"";
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();

        return createBatchController;
    }

    /**
     * shows the inputs ListView and Nodes relating to it
     * @param event
     */
    @FXML
    private void handleInputsToggleButtonPressed(ActionEvent event) {
        showFilesSection(false);
        showApplicationSection(false);
        showActionsSection(false);
        filesFinder1.setVisible(false);
        filesFinder2.setVisible(false);
        inputsToggleButton.setSelected(true);

        possibleCommandsListView.setVisible(true);
        possibleCommandsListView.setItems(inputCommands);
        possibleCommandsListView.setOnMouseClicked(mouseEvent -> commandField.setText(keyEvent + inputCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
    }

    /**
     * shows the applications ListView and Nodes relating to it
     * @param event
     */
    @FXML
    private void handleApplicationsToggleButtonPressed(ActionEvent event) {
        showFilesSection(false);
        showApplicationSection(true);
        showActionsSection(false);
        filesFinder1.setVisible(false);
        filesFinder2.setVisible(false);
        applicationsToggleButton.setSelected(true);

        initActionMap();

        applicationActionComboBox.setItems(FXCollections.observableArrayList(
                actionMap.keySet()
        ));
        applicationActionComboBox.getSelectionModel().select(5);

        ObservableList<String> applicationCommands = FXCollections.observableArrayList(
                applicationCommandsMap.keySet()
        );
        possibleCommandsListView.setItems(applicationCommands);
        possibleCommandsListView.getSelectionModel().select(selectedIndex);

        possibleCommandsListView.setOnMouseClicked(mouseEvent -> {
            if(possibleCommandsListView.getSelectionModel().getSelectedItem() != null) {
                commandField.setText(actionMap.get(applicationActionComboBox.getValue()) + " "
                        + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
                if (applicationActionComboBox.getSelectionModel().isSelected(5))
                    commandField.setText(commandField.getText() + " 1");

                selectedIndex = possibleCommandsListView.getSelectionModel().getSelectedIndex();
            }
        });
    }

    private boolean isCopyToAndroid = true;
    /**
     * shows the files ListView and Nodes relating to it
     * @param event
     */
    @FXML
    private void handleFilesToggleButtonPressed(ActionEvent event) {
        showFilesSection(true);
        showApplicationSection(false);
        showActionsSection(false);

        filesToggleButton.setSelected(true);
        filesFinder2.setVisible(false);

        ObservableList<String> selections = FXCollections.observableArrayList();
        selections.add("Copy file to Android");
        selections.add("Copy file from Android");
        selectionComboBox.getItems().clear();
        selectionComboBox.setItems(selections);

        selectionComboBox.getSelectionModel().select(0);
        selectionComboBox.setOnAction(event1 -> {
            filesTextField1.setText("");
            filesTextField2.setText("");

            isCopyToAndroid = selectionComboBox.getSelectionModel().isSelected(0);

            if(filesToggleButton.isSelected()) {
                filesFinder1.setVisible(isCopyToAndroid);
                filesFinder2.setVisible(!isCopyToAndroid);
            }

            filesLabel1.setText(isCopyToAndroid ? "Location on PC:" : "Location on Android:");
            filesLabel2.setText(!isCopyToAndroid ? "Location on PC:" : "Location on Android:");
        });
    }

    /**
     * shows the actions ListView and Nodes relating to it
     * @param event
     */
    @FXML
    private void handleActionsToggleButtonPressed(ActionEvent event) {
        actionsToggleButton.setSelected(true);
        showActionsSection(true);
        showFilesSection(false);
        showApplicationSection(false);
        filesFinder2.setVisible(false);
        selectionComboBox.setVisible(true);
        enterButton.setVisible(true);
        filesFinder1.setVisible(false);
        filesFinder2.setVisible(false);

        helpLabel.setOnAction(event1 -> browse("https://developer.android.com/studio/command-line/adb.html#IntentSpec"));
        actionLabel1.setOnAction(event1 -> browse("https://developer.android.com/guide/components/intents-filters.html#Types"));
        dataLabel.setOnAction(event1 -> browse("https://developer.android.com/guide/topics/manifest/data-element.html"));
        mimeTypeLabel.setOnAction(event1 -> browse("https://developer.android.com/guide/topics/manifest/data-element.html#mime"));
        categoryLabel.setOnAction(event1 -> browse("https://developer.android.com/guide/topics/manifest/category-element.html"));
        componentLabel.setOnAction(event1 -> browse("https://developer.android.com/guide/components/fundamentals.html#ActivatingComponents"));
        flagsLabel.setOnAction(event1 -> browse("https://developer.android.com/reference/android/content/Intent.html#setFlags(int)"));

        ObservableList<String> selections = FXCollections.observableArrayList();
        selections.add("Start Activity");
        selections.add("Start Service");
        selections.add("Send Broadcast");
        selectionComboBox.getItems().clear();
        selectionComboBox.setItems(selections);
        selectionComboBox.getSelectionModel().select(0);

        intentType = Intent.ACTIVITY;

        selectionComboBox.setOnAction(event1 -> {
            int index = selectionComboBox.getSelectionModel().getSelectedIndex();

            switch (index) {
                case 0:
                    intentType = Intent.ACTIVITY;
                    actionComboBox.getItems().clear();
                    actionComboBox.setItems(intentsValues());
                    break;
                case 1:
                    intentType = Intent.SERVICE;
                    actionComboBox.getItems().clear();
                    actionComboBox.setItems(intentsValues());
                    break;
                case 2:
                    intentType = Intent.BROADCAST;
                    actionComboBox.getItems().clear();
                    actionComboBox.setItems(broadcastValues());
                    break;
            }
        });

        actionComboBox.setOnAction(event1 -> actionTextField.setText(actionComboBox.getValue()));
        categoryComboBox.setOnAction(event1 -> categoryTextField.setText(categoryComboBox.getValue()));
        mimeTypeComboBox.setOnAction(event1 -> mimeTypeTextField.setText(mimeTypeComboBox.getValue()));
        componentComboBox.setOnAction(event1 -> componentTextField.setText(componentComboBox.getValue()));
    }

    /**
     * Displays a fileChooser allowing the user to select a file to send to the device
     * @param event
     */
    @FXML
    private void handleFilesFinder1Clicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to copy");
        fileChooser.setInitialDirectory(new File(FILES_DIRECTORY));

        Stage stage = (Stage) filesFinder1.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        filesTextField1.setText(file.getAbsolutePath());
    }

    /**
     * Displays a fileChooser allowing the user to select a folder to receive the file from the device
     * @param event
     */
    @FXML
    private void handleFilesFinder2Clicked(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose folder to copy file to");
        directoryChooser.setInitialDirectory(new File(FILES_DIRECTORY));

        Stage stage = (Stage) filesFinder1.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        filesTextField2.setText(selectedDirectory.getAbsolutePath());
    }

    /**
     * Adds commands relating to the selection to the commandField
     * @param event
     */
    @FXML
    private void handleEnterButtonClicked(ActionEvent event) {
        if(filesToggleButton.isSelected())
            commandField.setText((isCopyToAndroid ? "push " : "pull ") + filesTextField1.getText() + " " + filesTextField2.getText());
        else if(actionsToggleButton.isSelected()) {
            String action = " -a " + actionTextField.getText();
            String data = "";
            if(!dataTextField.getText().isEmpty()) data = " -d " + dataTextField.getText();
            String mimeType = "";
            if(!mimeTypeTextField.getText().isEmpty())  mimeType = " -t " + mimeTypeTextField.getText();
            String category = "";
            if(!categoryTextField.getText().isEmpty()) category = " -c " + categoryTextField.getText();
            String component = "";
            if(!componentTextField.getText().isEmpty()) component = " -n " + componentTextField.getText();
            String flags = "";
            if(!flagsTextField.getText().isEmpty()) flags = " -f " + flagsTextField.getText();

            switch (intentType) {
                case Intent.ACTIVITY:
                    commandField.setText("shell am start" + action + data + mimeType + category + component + flags);
                    break;
                case Intent.BROADCAST:
                    commandField.setText("shell am broadcast" + action + data + mimeType + category + component + flags);
                    break;
                case Intent.SERVICE:
                    commandField.setText("shell am startservice" + action + data + mimeType + category + component + flags);
                    break;
            }
        }
    }

    /**
     * Adds commands relating to the action to have on the selected application to the commandField
     * @param event
     */
    @FXML
    private void handleApplicationActionComboBoxClicked(ActionEvent event) {
        if (actionMap.get(applicationActionComboBox.getValue()) != null && possibleCommandsListView.getSelectionModel().getSelectedItem() !=  null) {
            commandField.setText(actionMap.get(applicationActionComboBox.getValue()) + " "
                    + applicationCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
            if (applicationActionComboBox.getSelectionModel().isSelected(5))
                commandField.setText(commandField.getText() + " 1");
        }
    }

    /**
     * Creates a new instance of {@link GetTouchPositionController} and displays it in a new window
     * @param event
     */
    @FXML
    private void handleGetCursorLocationClicked(ActionEvent event) {
        try {
            GetTouchPositionController getTouchPositionController = new GetTouchPositionController();
            getTouchPositionController.newWindow(this, null);
        } catch (IOException e) {
            Log.error(e.getMessage(), e);
        }
    }

    /**
     * Adds the text from the commandfield to the commands ListView
     * @param event
     */
    @FXML
    private void handleAddCommandButtonClicked(ActionEvent event) {
        if(!commandField.getText().isEmpty()) {
            saveButton.setDisable(false);
            commandsListView.getItems().add(indexBox.getValue(), commandField.getText());
            indexList.add(++index);
            indexBox.setItems(indexList);
            indexBox.setValue(index);
            indexListView.getItems().clear();
            for (int i = 0; i < indexBox.getValue(); i++) {
                indexListView.getItems().add(i);
            }
        }
    }

    /**
     * Deletes the selected command from the ListView
     * @param event
     */
    @FXML
    private void handleDeleteButtonClicked(ActionEvent event) {
        int commandsListViewIndex = commandsListView.getSelectionModel().getSelectedIndex();
        if (commandsListViewIndex > -1) {
            try {
                commandsListView.getItems().remove(commandsListViewIndex);
                indexListView.getItems().remove(commandsListViewIndex);
                indexList.remove(index--);
                indexBox.setItems(indexList);
                indexBox.setValue(index);
            } catch (Exception ee) {
                Log.error(ee.getMessage(), ee);
            }

            indexListView.getItems().clear();
            for (int i = 0; i < indexBox.getValue(); i++) {
                indexListView.getItems().add(i);
            }
        }
    }

    /**
     * Moves the selected command up the ListView
     * @param event
     */
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

    /**
     * Moves the selected command down the ListView
     * @param event
     */
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
        } catch (IndexOutOfBoundsException e) {
            Log.error(e.getMessage(), e);
        }
    }

    /**
     * Saves the commands from the ListView
     * If we are editing a file just save and exit
     * or else, show a test dialog to get the file name
     * @param event
     */
    @FXML
    private void handleSaveButtonClicked(ActionEvent event) {
        XMLUtil xmlUtil = new XMLUtil(false);

        if(editFile != null) {
            xmlUtil.saveBatchCommands(commandsListView.getItems(), editFile);
            editFile = null;
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save Batch Commands");
            dialog.setHeaderText("Enter Batch Name");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> xmlUtil.saveBatchCommands(commandsListView.getItems(), new File(DIRECTORY + "\\" + s + EXTENSION)));
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            Log.error(ie.getMessage(), ie);
        }

        controller.updateCommandsList();
        ((Stage) indexBox.getScene().getWindow()).close();
    }

    /**
     * Show a text input dialog to enter text to the device
     * @param event
     */
    @FXML
    private void handleEnterTextButtonClicked(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Text");
        dialog.setHeaderText("Type Text To Enter");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(s -> commandField.setText("shell input text " + s));
    }

    /**
     * Sets the text in the command field to the parameter
     * @param text
     */
    //ApplicationUtils
    public void setCommandText(String text) {
        commandField.setText(text);
    }

    public TextField getCommandField() {
        return commandField;
    }

    /**
     * Creates an ObservableList<String> of broadcastable actions
     * @return ObservableList<String> of broadcastable actions
     */
    private ObservableList<String> broadcastValues() {
        ObservableList<String> values = FXCollections.observableArrayList();
        values.add("android.intent.action.TIME_TICK");
        values.add("android.intent.action.TIME_CHANGED");
        values.add("android.intent.action.TIMEZONE_CHANGED");
        values.add("android.intent.action.BOOT_COMPLETED");
        values.add("android.intent.action.PACKAGE_ADDED");
        values.add("android.intent.action.PACKAGE_CHANGED");
        values.add("android.intent.action.PACKAGE_REMOVED");
        values.add("android.intent.action.PACKAGE_RESTARTED");
        values.add("android.intent.action.PACKAGE_DATA_CLEARED");
        values.add("android.intent.action.PACKAGES_SUSPENDED");
        values.add("android.intent.action.PACKAGES_UNSUSPENDED");
        values.add("android.intent.action.UID_REMOVED");
        values.add("android.intent.action.BATTERY_CHANGED");
        values.add("android.intent.action.POWER_CONNECTED");
        values.add("android.intent.action.POWER_DISCONNECTED");
        values.add("android.intent.action.SHUTDOWN");

        return values;
    }

    /**
     * Creates an ObservableList<String> of actions
     * @return ObservableList<String> of actions
     */
    private ObservableList<String> intentsValues() {
        ObservableList<String> values = FXCollections.observableArrayList();
        values.add("android.intent.action.MAIN");
        values.add("android.intent.action.VIEW");
        values.add("android.intent.action.ATTACH_DATA");
        values.add("android.intent.action.EDIT");
        values.add("android.intent.action.PICK");
        values.add("android.intent.action.CHOOSER");
        values.add("android.intent.action.GET_CONTENT");
        values.add("android.intent.action.DIAL");
        values.add("android.intent.action.CALL");
        values.add("android.intent.action.SEND");
        values.add("android.intent.action.SENDTO");
        values.add("android.intent.action.ANSWER");
        values.add("android.intent.action.INSERT");
        values.add("android.intent.action.DELETE");
        values.add("android.intent.action.RUN");
        values.add("android.intent.action.SYNC");
        values.add("android.intent.action.PICK_ACTIVITY");
        values.add("android.intent.action.SEARCH");
        values.add("android.intent.action.WEB_SEARCH");
        values.add("android.intent.action.FACTORY_TEST");

        return values;
    }

    /**
     * Displays or hides Nodes relating to the action section
     * @param flag
     */
    private void showActionsSection(boolean flag) {
        helpLabel.setVisible(flag);
        actionComboBox.setVisible(flag);
        categoryComboBox.setVisible(flag);
        mimeTypeComboBox.setVisible(flag);
        componentComboBox.setVisible(flag);
        actionLabel1.setVisible(flag);
        dataLabel.setVisible(flag);
        mimeTypeLabel.setVisible(flag);
        categoryLabel.setVisible(flag);
        componentLabel.setVisible(flag);
        flagsLabel.setVisible(flag);
        actionTextField.setVisible(flag);
        dataTextField.setVisible(flag);
        mimeTypeTextField.setVisible(flag);
        categoryTextField.setVisible(flag);
        componentTextField.setVisible(flag);
        flagsTextField.setVisible(flag);
    }

    /**
     * Displays or hides Nodes relating to the application section
     * @param flag
     */
    private void showApplicationSection(boolean flag) {
        possibleCommandsListView.setVisible(flag);
        applicationActionComboBox.setVisible(flag);
        actionLabel.setVisible(flag);
    }

    /**
     * Displays or hides Nodes relating to the files section
     * @param flag
     */
    private void showFilesSection(boolean flag) {
        possibleCommandsListView.setVisible(!flag);
        selectionComboBox.setVisible(flag);
        filesFinder1.setVisible(flag);
        filesLabel1.setVisible(flag);
        filesLabel2.setVisible(flag);
        filesTextField1.setVisible(flag);
        filesTextField2.setVisible(flag);
        enterButton.setVisible(flag);
    }

    /**
     * Initializes the Action HashMap
     */
    private void initActionMap() {
        actionMap = new HashMap<>();
        actionMap.put("Disable App", "shell pm disable");
        actionMap.put("Enable App", "shell pm enable");
        actionMap.put("Clear App Data", "shell pm clear");
        actionMap.put("Uninstall", "shell pm uninstall");
        actionMap.put("Close", "shell am force-stop");
        actionMap.put("Open", "shell monkey -p");
    }

    /**
     * Initializes the Application HashMap
     */
    private void initApplicationCommandsMap() {
        applicationCommandsMap = new HashMap<>();

        ArrayList<String> applications = ADBUtil.listApplications();
        for(String application : applications) {
            applicationCommandsMap.put(application, application);
            componentComboBox.getItems().add(application);
        }
    }

    /**
     * Initializes the Input HashMap
     */
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
        ToggleGroup toggleGroup = new ToggleGroup();
        inputsToggleButton.setToggleGroup(toggleGroup);
        applicationsToggleButton.setToggleGroup(toggleGroup);
        filesToggleButton.setToggleGroup(toggleGroup);
        actionsToggleButton.setToggleGroup(toggleGroup);

        inputsToggleButton.setSelected(true);

        setImage("/resources/right.png", "Add command to the list", addCommandButton);
        setImage("/resources/up.png", "Move command up the list", moveUpButton);
        setImage("/resources/down.png","Move command down the list", moveDownButton);
        setImage("/resources/delete.png", "Delete command", deleteButton);
        setImage("/resources/open_folder.png", null, filesFinder1);
        setImage("/resources/open_folder.png", null, filesFinder2);
        setImage("/resources/enter.png", null, enterButton);
    }
}
