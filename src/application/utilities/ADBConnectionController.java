package application.utilities;

import application.main.FXMLMainController;
import application.device.Device;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ADBConnectionController implements Initializable, Showable<FXMLMainController> {
    private static final Logger Log = Logger.getLogger(ADBConnectionController.class.getName());

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

    private Device device = Device.getInstance();

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
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
        String deviceName = devicesListView.getSelectionModel().getSelectedItem();
        device.setName(deviceName);

        resultLabel.setTextFill(Color.GREEN);
        resultLabel.setText("Connected to " + device);

        OKButton.setVisible(true);
    }

    @FXML
    private void handleConnectWifiClicked() {
        String deviceName = devicesListView.getSelectionModel().getSelectedItem();
        device.setName(deviceName);

        resultLabel.setText("Connecting...");

        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                return device.connectOverWifi();
            }
        };

        task.setOnSucceeded(event -> {
            switch (task.getValue()) {
                case 0:
                    resultLabel.setTextFill(Color.GREEN);
                    resultLabel.setText("Connected to " + device + "\n" +
                            "You can disconnect it from the USB port");

                    OKButton.setVisible(true);
                    break;
                case 1:
                    resultLabel.setTextFill(Color.GREEN);
                    resultLabel.setText("Already connected to " + device);

                    OKButton.setVisible(true);
                    break;
                case 2:
                    resultLabel.setTextFill(Color.RED);
                    resultLabel.setText("Could not connect to " + device);
                    break;
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
        device.handleNewConnection();
        ((Stage) connectButton.getScene().getWindow()).close();
    }

    @Override
    public Initializable newWindow(FXMLMainController controller, Object object) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ADBConnectionController.class.getClass().getResource("/application/utilities/ADBConnection.fxml"));

        Parent root = fxmlLoader.load();
        ADBConnectionController adbConnectionController = fxmlLoader.getController();
        root.getStylesheets().add("/application/main/global.css");

        Stage stage = new Stage();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(root));
        stage.show();

        return adbConnectionController;
    }
}
