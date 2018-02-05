package application.phone;

import application.TelnetServer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class PhoneTabController implements Initializable {

    @FXML
    private TextField phoneNumberField;

    @FXML
    private Button makeCall;
    @FXML
    private Button holdCall;
    @FXML
    private Button endCall;
    @FXML
    private Button sendSMS;

    @FXML
    private TextArea messageArea;

    @FXML
    private ComboBox networkType;
    @FXML
    private ComboBox signalStrength;
    @FXML
    private ComboBox voiceStatus;
    @FXML
    private ComboBox dataStatus;

    private String phoneNumber;

    private boolean isInCall = false;
    private boolean isOnHold = false;
    private boolean isInteger = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        holdCall.setDisable(true);
        endCall.setDisable(true);
        makeCall.setDisable(true);
        sendSMS.setDisable(true);

        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!phoneNumberField.getText().isEmpty()) {
                try {
                    Integer.parseInt(phoneNumberField.getText());
                    isInteger = true;
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    isInteger = false;
                }

                if(isInteger)
                    makeCall.setDisable(false);
                else makeCall.setDisable(true);
            }
            else makeCall.setDisable(true);
        });

        messageArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!phoneNumberField.getText().isEmpty())
                sendSMS.setDisable(false);
            else sendSMS.setDisable(true);
        });

        makeCall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                phoneNumber = phoneNumberField.getText();
                if(!phoneNumber.isEmpty()) {
                    TelnetServer.makeCall(phoneNumber);
                    isInCall = true;
                    makeCall.setDisable(true);
                    holdCall.setDisable(false);
                    endCall.setDisable(false);
                }
            }
        });

        holdCall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!isOnHold) {
                    TelnetServer.holdCall(phoneNumber);
                    isOnHold = true;
                    holdCall.setText("Un-Hold");
                } else {
                    TelnetServer.unHoldCall(phoneNumber);
                    isOnHold = false;
                    holdCall.setText("Hold");
                }
            }
        });

        endCall.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               TelnetServer.endCall(phoneNumber);
               makeCall.setDisable(false);
               endCall.setDisable(true);
               holdCall.setDisable(true);
            }
        });

        sendSMS.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                phoneNumber  = phoneNumberField.getText();
                String command =  messageArea.getText().replaceAll("\n", "\\\\n");
                TelnetServer.sendSMS(phoneNumber + " " + command);
            }
        });

        networkType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String network = networkType.getValue().toString().toLowerCase();
                System.out.println(network);
                TelnetServer.networkSpeed(network);
            }
        });

        signalStrength.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String signal = "";
                switch (signalStrength.getValue().toString().toLowerCase()) {
                    case "none":
                        signal = "0";
                        break;
                    case "poor":
                        signal = "1";
                        break;
                    case "moderate":
                        signal = "2";
                        break;
                    case "good":
                        signal = "3";
                        break;
                    case "great":
                        signal = "4";
                        break;
                }
                System.out.println(signal);
                TelnetServer.gsmSignal(signal);
            }
        });

        voiceStatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String voice = voiceStatus.getValue().toString().toLowerCase();
                System.out.println(voice);
                TelnetServer.voiceStatus(voice);
            }
        });

        dataStatus.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String data = dataStatus.getValue().toString().toLowerCase();
                System.out.println(data);
                TelnetServer.dataStatus(data);
            }
        });
    }
}
