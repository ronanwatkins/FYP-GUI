package application.utilities;

import application.ADBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ADBConnectionController implements Initializable {

    @FXML
    private Button connectButton;
    @FXML
    private Button connectWifiButton;

    @FXML
    private ListView<String> devicesListView;

    @FXML
    private ObservableList<String> devicesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectButton.setDisable(true);
        connectWifiButton.setDisable(true);

        devicesListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                connectButton.setDisable(false);

                if(devicesListView.getSelectionModel().getSelectedItem().contains("emulator"))
                    connectWifiButton.setDisable(true);
                else
                    connectWifiButton.setDisable(false);
            }
        });
    }

    public void initDevices(String[] devices) {
        devicesList = FXCollections.observableArrayList();

        for(String device : devices) {
            if(!device.contains("List of devices attached"))
                devicesList.add(device.replace("device" ,"").trim());
        }

        devicesListView.setItems(devicesList);
    }

    @FXML
    private void handleConnectButtonClicked() {
        ADBUtil.setDeviceName(devicesListView.getSelectionModel().getSelectedItem());

//        synchronized (ADBUtil.lock) {
//            ADBUtil.lock.notify();
//        }

        ((Stage) connectButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleConnectWifiClicked() {
        ADBUtil.connectOverWifi(devicesListView.getSelectionModel().getSelectedItem());

//        synchronized (ADBUtil.lock) {
//            ADBUtil.lock.notify();
//        }

        ((Stage) connectButton.getScene().getWindow()).close();
    }
}
