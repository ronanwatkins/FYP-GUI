package application.commands;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.*;

public class CommandsTabController implements Initializable {
    
    public static final String DIRECTORY = System.getProperty("user.dir") + "\\misc\\commands";

    @FXML
    public TabPane tabPane;

    @FXML
    public Tab runBatchTab;
    @FXML
    public Tab createBatchTab;

    @FXML
    public CreateBatchTabController createBatchTabController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createBatchTab.setOnSelectionChanged(event -> {
            if (createBatchTab.isSelected()) {
                System.out.println("Create Batch Tab Selected");
            }
        });

        runBatchTab.setOnSelectionChanged(event -> {
            if (runBatchTab.isSelected()) {
                System.out.println("Run Batch Tab Selected");
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {
            if (newValue == runBatchTab) {
                System.out.println("Run Batch Tab Selected: " );
                tabPane.getSelectionModel().select(runBatchTab);
            } else if (newValue == createBatchTab) {
                System.out.println("Create Batch Tab Selected: " );
                tabPane.getSelectionModel().select(createBatchTab);
            }
        });
    }
}
