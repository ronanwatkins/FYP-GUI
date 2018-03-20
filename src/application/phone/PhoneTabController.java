package application.phone;

import application.TelnetServer;
import javafx.event.ActionEvent;
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
    private Button makeCallButton;
    @FXML
    private Button holdCallButton;
    @FXML
    private Button endCallButton;
    @FXML
    private Button sendSMSButton;

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
        networkType.getSelectionModel().select(2);
        signalStrength.getSelectionModel().select(4);
        voiceStatus.getSelectionModel().select(0);
        dataStatus.getSelectionModel().select(0);

        holdCallButton.setDisable(true);
        endCallButton.setDisable(true);
        makeCallButton.setDisable(true);
        sendSMSButton.setDisable(true);

        handleTextAreaEvents();
        handleComboBoxEvents();
    }

    private void handleTextAreaEvents() {
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!phoneNumberField.getText().isEmpty()) {
                try {
                    Integer.parseInt(phoneNumberField.getText());
                    isInteger = true;
                } catch (NumberFormatException nfe) {
                    //nfe.printStackTrace();
                    isInteger = false;
                }

                if(isInteger)
                    makeCallButton.setDisable(false);
                else makeCallButton.setDisable(true);
            }
            else makeCallButton.setDisable(true);
        });

        messageArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!phoneNumberField.getText().isEmpty())
                sendSMSButton.setDisable(false);
            else sendSMSButton.setDisable(true);
        });
    }

    private void handleComboBoxEvents() {
        networkType.setOnAction(event -> {
            String network = networkType.getValue().toString().toLowerCase();
            System.out.println(network);
            TelnetServer.networkSpeed(network);
        });

        signalStrength.setOnAction(event -> {
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
        });

        voiceStatus.setOnAction(event -> {
            String voice = voiceStatus.getValue().toString().toLowerCase();
            System.out.println(voice);
            TelnetServer.voiceStatus(voice);
        });

        dataStatus.setOnAction(event -> {
            String data = dataStatus.getValue().toString().toLowerCase();
            System.out.println(data);
            TelnetServer.dataStatus(data);
        });
    }

    @FXML
    private void handleMakeCallButtonClicked(ActionEvent event) {
        phoneNumber = phoneNumberField.getText();
        if(!phoneNumber.isEmpty()) {
            TelnetServer.makeCall(phoneNumber);
            isInCall = true;
            makeCallButton.setDisable(true);
            holdCallButton.setDisable(false);
            endCallButton.setDisable(false);
        }
    }

    @FXML
    private void handleEndCallButtonClicked(ActionEvent event) {
        TelnetServer.endCall(phoneNumber);
        makeCallButton.setDisable(false);
        endCallButton.setDisable(true);
        holdCallButton.setDisable(true);
    }

    @FXML
    private void handleHoldCallButtonClicked(ActionEvent event) {
        if(!isOnHold) {
            TelnetServer.holdCall(phoneNumber);
            isOnHold = true;
            holdCallButton.setText("Un-Hold");
        } else {
            TelnetServer.unHoldCall(phoneNumber);
            isOnHold = false;
            holdCallButton.setText("Hold");
        }
    }

    @FXML
    private void handleSendSMSButtonClicked(ActionEvent event) {
        phoneNumber  = phoneNumberField.getText();
        String command =  messageArea.getText().replaceAll("\n", "\\\\n");
        TelnetServer.sendSMS(phoneNumber + " " + command);
    }
}
