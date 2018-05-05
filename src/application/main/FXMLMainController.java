package application.main;

import application.monitor.MonitorTabController;
import application.utilities.ADBConnectionController;
import application.utilities.ADBUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FXMLMainController implements Initializable {
    private static final Logger Log = Logger.getLogger(FXMLMainController.class.getName());

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
    @FXML
    private Tab monitorTab;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        monitorTab.setOnSelectionChanged(event -> {
            if(monitorTab.isSelected())
                MonitorTabController.getController().resume();
            else
                MonitorTabController.getController().pause();
        });

        sensorsTab.setGraphic(setIcon("/resources/sensor.png", 40, 40));
        locationTab.setGraphic(setIcon("/resources/location.jpg", 15, 15));
        phoneTab.setGraphic(setIcon("/resources/phone.png",15, 15));
        consoleTab.setGraphic(setIcon("/resources/console.png", 15, 15));
        automationTab.setGraphic(setIcon("/resources/automation.png", 12, 12));
        applicationsTab.setGraphic(setIcon("/resources/application.jpg ", 15, 15));
        logCatTab.setGraphic(setIcon("/resources/logcat.png", 15, 15));
        monitorTab.setGraphic(setIcon("/resources/monitor.png", 20, 20));
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
            Log.error(ioe.getMessage(), ioe);
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
