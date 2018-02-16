package application.commands;

import application.XMLUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommandsTabController implements Initializable{

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
    private Tab runBatchTab;
    @FXML
    private Tab createBatchTab;

    @FXML
    private TabPane tabPane;

    @FXML
    private ChoiceBox<Integer> indexBox;

    private HashMap<String, String> commandsMap;

    private int index = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        String keyEvent = "shell input keyevent ";


        initCommandsMap();

        ObservableList<String> possibleCommands = FXCollections.observableArrayList(
                commandsMap.keySet()
        );

        ObservableList<Integer> indexList = FXCollections.observableArrayList(
            index
        );

        deleteButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);
        saveButton.setDisable(true);

        indexBox.setItems(indexList);
        indexBox.setValue(index);

        possibleCommandsListView.setItems(possibleCommands);

        possibleCommandsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                commandField.setText(keyEvent + commandsMap.get(possibleCommandsListView.getSelectionModel().getSelectedItem()));
            }
        });

        indexBox.setItems(indexList);

        addCommandButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!commandField.getText().isEmpty()) {
                    saveButton.setDisable(false);
                    commandsListView.getItems().add(indexBox.getValue(), commandField.getText());

                    index+=1;
                    indexList.add(index);
                    indexBox.setItems(indexList);
                    indexBox.setValue(index);

                    indexListView.getItems().clear();
                    for (int i = 0; i < indexBox.getValue(); i++) {
                        indexListView.getItems().add(i);
                    }
                }
            }
        });

        commandsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (!commandsListView.getSelectionModel().getSelectedItem().isEmpty()) {
                        deleteButton.setDisable(false);
                        moveDownButton.setDisable(false);
                        moveUpButton.setDisable(false);
                    }
                } catch (NullPointerException npe) {
                    //npe.printStackTrace();
                }
            }
        });

        moveUpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int selectedItemIndex = commandsListView.getSelectionModel().getSelectedIndex();
                if(selectedItemIndex > 0) {
                    String selectedItemValue = commandsListView.getSelectionModel().getSelectedItem();
                    commandsListView.getItems().remove(selectedItemIndex);
                    commandsListView.getItems().add(selectedItemIndex - 1, selectedItemValue);

                    commandsListView.getSelectionModel().select(selectedItemIndex - 1);
                }
            }
        });

        moveDownButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
        });

        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                commandsListView.getItems().remove(commandsListView.getSelectionModel().getSelectedIndex());
                indexListView.getItems().remove(commandsListView.getSelectionModel().getSelectedIndex());

                indexList.remove(index);
                index -= 1;

                indexBox.setItems(indexList);
                indexBox.setValue(index);

                indexListView.getItems().clear();
                for (int i = 0; i < indexBox.getValue(); i++) {
                    indexListView.getItems().add(i);
                }
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Save Batch Commands");
                dialog.setHeaderText("Enter Command Name");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    System.out.println(result.get());
                    XMLUtil xmlUtil = new XMLUtil();
                    xmlUtil.saveBatchCommands(commandsListView.getItems());
                    tabPane.getSelectionModel().select(runBatchTab);
                }
            }
        });

//        ScrollBar scrollBarOne;
//        ScrollBar scrollBarTwo;
//
//
//        // lookup after scene rendering....
//        indexListView.setStyle(".scroll-bar:vertical");
//        commandsListView.setStyle(".scroll-bar:vertical");
//        System.out.println(indexListView.lookup(".scroll-bar:vertical"));
//        System.out.println(commandsListView.lookup(".scroll-bar:vertical"));
//        scrollBarOne = (ScrollBar) indexListView.lookup(".scroll-bar:vertical");
//        scrollBarTwo = (ScrollBar) commandsListView.lookup(".scroll-bar:vertical");
//
//        scrollBarOne.valueProperty().bindBidirectional(scrollBarTwo.valueProperty());

    }

    private void initCommandsMap() {
        commandsMap = new HashMap<>();
        commandsMap.put("Back", "KEYCODE_BACK");
        commandsMap.put("Home", "KEYCODE_HOME");
        commandsMap.put("List Open Apps", "KEYCODE_APP_SWITCH");
        commandsMap.put("Volume Up", "KEYCODE_VOLUME_UP");
        commandsMap.put("Volume Down", "KEYCODE_VOLUME_DOWN");
        commandsMap.put("Power", "KEYCODE_POWER");
        commandsMap.put("Tab", "KEYCODE_TAB");
        commandsMap.put("Enter", "KEYCODE_ENTER");
    }
}
