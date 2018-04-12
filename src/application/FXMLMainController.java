package application;

import application.utilities.ADBConnectionController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FXMLMainController implements Initializable {

    @FXML
    private BorderPane borderPane;

    @FXML
    private Tab locationTab;
    @FXML
    private Tab phoneTab;
    @FXML
    private Tab consoleTab;
    @FXML
    private Tab automationTab;
    @FXML
    private Tab applicationsTab;
    @FXML
    private Tab logCatTab;
    @FXML
    private Tab sensorsTab;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sensorsTab.setGraphic(setIcon("/resources/sensor.png", 40, 40));
        locationTab.setGraphic(setIcon("/resources/location.jpg", 15, 15));
        phoneTab.setGraphic(setIcon("/resources/phone.png",15, 15));
        consoleTab.setGraphic(setIcon("/resources/console.png", 15, 15));
        automationTab.setGraphic(setIcon("/resources/automation.png", 15, 15));
        applicationsTab.setGraphic(setIcon("/resources/application.jpg ", 15, 15));
        logCatTab.setGraphic(setIcon("/resources/logcat.png", 15, 15));
    }

    private ImageView setIcon(String URL, int width, int height) {
        Image image = new Image(URL, width, height, true, true);
        return new ImageView(image);
    }

    @FXML
    private void handleConnectionItemClicked(ActionEvent event) {
        ADBConnectionController adbConnectionController = new ADBConnectionController();
        try {
            adbConnectionController = (ADBConnectionController) adbConnectionController.newWindow(this, null);
            adbConnectionController.initDevices(ADBUtil.connectedDevices());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @FXML
    private void handleExitItemClicked(ActionEvent event) {
        Main.exit();
    }

    @FXML
    private void handleOpenItemClicked(ActionEvent event) {
        Main.hostServices().showDocument(Main.APPLICATION_DIRECTORY);
    }
}
