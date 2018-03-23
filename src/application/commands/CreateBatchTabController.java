package application.commands;

import application.ADBUtil;
import application.XMLUtil;
import application.commands.extras.GetTouchPositionController;
import application.commands.extras.RecordInputsController;
import application.utilities.Utilities;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

import static application.commands.CommandsTabController.DIRECTORY;

public class CreateBatchTabController implements Initializable {

    private final String FILES_DIRECTORY = System.getProperty("user.dir") + "\\misc";

    @FXML
    private TextField commandField;
    @FXML
    private TextField filesTextField1;
    @FXML
    private TextField filesTextField2;

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
    private Button enterTextButton;
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
    private Label actionLabel;
    @FXML
    private Label filesLabel1;
    @FXML
    private Label filesLabel2;

    @FXML
    private ComboBox<String> actionComboBox;
    @FXML
    private ComboBox filesComboBox;

    @FXML
    private ChoiceBox<Integer> indexBox;

    private static CommandsTabController controller;

    private int index = 0;
    private int selectedIndex = 0;

    private static File editFile;

    private final String keyEvent = "shell input keyevent ";

    private HashMap<String, String> inputCommandsMap;
    private HashMap<String, String> applicationCommandsMap;
    private HashMap<String, String> actionMap;

    private ObservableList<String> inputCommands;
    private ObservableList<Integer> indexList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showFilesSection(false);
        initApplicationCommandsMap();
        initializeButtons();
        initInputCommandsMap();

        if(editFile != null) {
            saveButton.setDisable(false);
            XMLUtil xmlUtil = new XMLUtil();

            ObservableList<String> commands = xmlUtil.openBatchCommands(editFile);
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

    public static void showScreen(CommandsTabController commandsTabController, File file) throws IOException {
        controller = commandsTabController;
        editFile = file;

        FXMLLoader fxmlLoader = new FXMLLoader(commandsTabController.getClass().getResource("/application/commands/CreateBatchTabView.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        String title = file == null ? "Create Batch Commands" : "Editing \"" + file.getName().replace(".xml", "") + "\"";
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleInputsButtonPressed(ActionEvent event) {
        showFilesSection(false);
        showApplicationSection(false);
        possibleCommandsListView.setVisible(true);
        inputsToggleButton.setSelected(true);

        possibleCommandsListView.setItems(inputCommands);
        possibleCommandsListView.setOnMouseClicked(mouseEvent -> commandField.setText(keyEvent + inputCommandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem())));
    }

    @FXML
    private void handleApplicationsButtonPressed(ActionEvent event) {
        showFilesSection(false);
        showApplicationSection(true);
        possibleCommandsListView.setVisible(true);
        applicationsToggleButton.setSelected(true);

        initActionMap();

        actionComboBox.setItems(FXCollections.observableArrayList(
                actionMap.keySet()
        ));
        actionComboBox.getSelectionModel().select(5);

        ObservableList<String> applicationCommands = FXCollections.observableArrayList(
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
    }

    boolean isCopyToAndroid = true;
    @FXML
    private void handleFilesToggleButtonPressed(ActionEvent event) {
        showFilesSection(true);
        showApplicationSection(false);

        filesToggleButton.setSelected(true);
        possibleCommandsListView.setVisible(false);
        filesFinder2.setVisible(false);

        filesComboBox.getSelectionModel().select(0);
        filesComboBox.setOnAction(event1 -> {
            filesTextField1.setText("");
            filesTextField2.setText("");

            isCopyToAndroid = filesComboBox.getSelectionModel().isSelected(0);

            filesFinder1.setVisible(isCopyToAndroid);
            filesFinder2.setVisible(!isCopyToAndroid);
            filesLabel1.setText(isCopyToAndroid ? "Location on PC:" : "Location on Android:");
            filesLabel2.setText(!isCopyToAndroid ? "Location on PC:" : "Location on Android:");
        });
    }

    @FXML
    private void handleFilesFinder1Clicked(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to copy");
        fileChooser.setInitialDirectory(new File(FILES_DIRECTORY));

        Stage stage = (Stage) filesFinder1.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        filesTextField1.setText(file.getAbsolutePath());
    }

    @FXML
    private void handleFilesFinder2Clicked(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose folder to copy file to");
        directoryChooser.setInitialDirectory(new File(FILES_DIRECTORY));

        Stage stage = (Stage) filesFinder1.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        filesTextField2.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    private void handleEnterButtonClicked(ActionEvent event) {
        commandField.setText((isCopyToAndroid ? "push " : "pull ") + filesTextField1.getText() + " " + filesTextField2.getText());
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
            if(indexList.get(0) == 0 && indexList.get(1) == 0)
                indexList.remove(0);
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
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save Batch Commands");
            dialog.setHeaderText("Enter Batch Name");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println(result.get());

                xmlUtil.saveBatchCommands(commandsListView.getItems(), new File(DIRECTORY + "\\" + result.get() + ".xml"));
            }
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        controller.updateCommandsList();
        ((Stage) indexBox.getScene().getWindow()).close();
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

    private void showApplicationSection(boolean flag) {
        actionComboBox.setVisible(flag);
        actionLabel.setVisible(flag);
    }

    private void showFilesSection(boolean flag) {
        filesComboBox.setVisible(flag);
        filesFinder1.setVisible(flag);
        filesFinder2.setVisible(flag);
        filesLabel1.setVisible(flag);
        filesLabel2.setVisible(flag);
        filesTextField1.setVisible(flag);
        filesTextField2.setVisible(flag);
        enterButton.setVisible(flag);
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

    public void initApplicationCommandsMap() {
        System.out.println("initApplicationCommandsMap>> ");
        applicationCommandsMap = new HashMap<>();

        System.out.println("going to listApplications");
        ArrayList<String> applications = ADBUtil.listApplications();
        System.out.println("finished listApplications");
        for(String application : applications) {
            System.out.println("application: " + application);
            applicationCommandsMap.put(application, application);
        }
        System.out.println("Done");
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

    private void initializeButtons() {
        ToggleGroup toggleGroup = new ToggleGroup();
        inputsToggleButton.setToggleGroup(toggleGroup);
        applicationsToggleButton.setToggleGroup(toggleGroup);
        filesToggleButton.setToggleGroup(toggleGroup);

        inputsToggleButton.setSelected(true);

        Utilities.setImage("/resources/right.png", addCommandButton);
        Utilities.setImage("/resources/up.png", moveUpButton);
        Utilities.setImage("/resources/down.png", moveDownButton);
        Utilities.setImage("/resources/delete.png", deleteButton);
        Utilities.setImage("/resources/open_folder.png", filesFinder1);
        Utilities.setImage("/resources/open_folder.png", filesFinder2);
        Utilities.setImage("/resources/enter.png", enterButton);
    }
}
