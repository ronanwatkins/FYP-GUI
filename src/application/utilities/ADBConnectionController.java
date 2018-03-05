package application.utilities;

import application.ADBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ADBConnectionController implements Initializable {

    @FXML
    private Button connectButton;
    @FXML
    private Button connectWifiButton;
    @FXML
    private Button OKButton;

    @FXML
    private ListView<String> devicesListView;

    @FXML
    private Label resultLabel;

    @FXML
    private ObservableList<String> devicesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        connectButton.setDisable(true);
        connectWifiButton.setDisable(true);
        OKButton.setVisible(false);

        devicesListView.setOnMouseClicked(event -> {
            connectButton.setDisable(false);
            if(devicesListView.getSelectionModel().getSelectedIndex() > -1) {
                if (devicesListView.getSelectionModel().getSelectedItem().contains("emulator"))
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
                devicesList.add(device.replace("\t" ," ").split(" ")[0].trim());
        }

        devicesListView.getItems().clear();
        devicesListView.setItems(devicesList);
        connectButton.setDisable(true);
        connectWifiButton.setDisable(true);
    }

    @FXML
    private void handleConnectButtonClicked() {
        String device = devicesListView.getSelectionModel().getSelectedItem();
        ADBUtil.setDeviceName(device);

        resultLabel.setTextFill(Color.GREEN);
        resultLabel.setText("Connected to " + device);

        OKButton.setVisible(true);
    }

    @FXML
    private void handleConnectWifiClicked() {
        String device = devicesListView.getSelectionModel().getSelectedItem();
        resultLabel.setText("Connecting...");

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return ADBUtil.connectOverWifi(device);
            }
        };

        task.setOnSucceeded(event -> {
            String result = task.getValue();
            System.out.println("RESULT: " + result);
            if(result.startsWith("connected")) {
                resultLabel.setTextFill(Color.GREEN);
                resultLabel.setText("Connected to " + device + "\n" +
                        "You can disconnect it from the USB port");

                OKButton.setVisible(true);
            } else if(result.startsWith("already")) {
                resultLabel.setTextFill(Color.GREEN);
                resultLabel.setText("Already connected to " + device);

                OKButton.setVisible(true);
            } else {
                resultLabel.setTextFill(Color.RED);
                resultLabel.setText("Could not connect to " + device);
            }
        });

        task.setOnFailed(event -> {
            resultLabel.setTextFill(Color.RED);
            resultLabel.setText("Could not connect to " + device);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleOKButtonClicked(ActionEvent event) {
        ((Stage) connectButton.getScene().getWindow()).close();
    }
}
