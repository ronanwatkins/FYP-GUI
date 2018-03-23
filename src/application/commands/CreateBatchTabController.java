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
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public class CreateBatchTabController implements Initializable {

    @FXML
    private TextField commandField;

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
    private TabPane tabPane;

    @FXML
    private ChoiceBox<Integer> indexBox;

    private int index = 0;
    private int selectedIndex = 0;

    private final String keyEvent = "shell input keyevent ";

    private HashMap<String, String> inputCommandsMap;
    private HashMap<String, String> applicationCommandsMap;
    private HashMap<String, String> actionMap;

    private ObservableList<String> inputCommands;
    private ObservableList<String> applicationCommands;

    private ObservableList<Integer> indexList;

    //@FXML
   // private RunBatchTabController runBatchTabController;

    private static File editFile;

    public static void setEditFile(File file) {
        editFile = file;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //stopCommandsButton.fire();
        //runBatchTabController.stopCommandsButton.fire();



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
        System.out.println("handleApplicationsButtonPressed >>");
        initApplicationCommandsMap();
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



            //tabPane.getSelectionModel().select(runBatchTab);
//            runBatchTabController.init();
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Save Batch Commands");
            dialog.setHeaderText("Enter Batch Name");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                System.out.println(result.get());

                xmlUtil.saveBatchCommands(commandsListView.getItems(), new File(CommandsTabController.DIRECTORY + "\\" + result.get() + ".xml"));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                } finally {
                    //tabPane.getSelectionModel().select(runBatchTab);
//                    runBatchTabController.init();



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
        inputsToggleButton.setSelected(true);

        Utilities.setImage("/resources/right.png", addCommandButton);
        Utilities.setImage("/resources/up.png", moveUpButton);
        Utilities.setImage("/resources/down.png", moveDownButton);
        Utilities.setImage("/resources/delete.png", deleteButton);
    }
}
