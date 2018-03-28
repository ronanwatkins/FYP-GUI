package application;

import application.applications.ApplicationTabController;
import application.automation.AutomationTabController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import application.sensors.SensorsTabController;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

public class FXMLMainController implements Initializable {

    @FXML
    private TabPane tabPane;

    private ApplicationTabController applicationTabController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        applicationTabController = new ApplicationTabController();

        tabPane.setOnMouseClicked(event -> {
            if(tabPane.getSelectionModel().isSelected(5)) {
                System.out.println("selected");
//                applicationTabController.updateDeviceListView();
            }
        });
    }
}
