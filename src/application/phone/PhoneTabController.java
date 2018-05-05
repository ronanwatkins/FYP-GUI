package application.phone;

import application.utilities.TelnetServer;
import application.utilities.ApplicationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class PhoneTabController implements Initializable, ApplicationUtils {

    @FXML
    private Slider batterySlider;

    @FXML
    private Label batteryLabel;

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
    @FXML
    private ComboBox batteryHealth;
    @FXML
    private ComboBox batteryStatus;
    @FXML
    private ComboBox charging;

    private String phoneNumber;

    private boolean isOnHold = false;
    private boolean isInteger = false;

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();

        batterySlider.setValue(100);
        batteryHealth.getSelectionModel().select(2);
        batteryStatus.getSelectionModel().select(3);
        charging.getSelectionModel().select(0);

        batterySlider.valueProperty().addListener((observable, oldvalue, newvalue) ->
        {
            int batteryValue = newvalue.intValue();
            batteryLabel.setText(batteryValue+"");
            TelnetServer.powerCapacity(batteryValue+"");
        });

        networkType.getSelectionModel().select(2);
        signalStrength.getSelectionModel().select(4);
        voiceStatus.getSelectionModel().select(0);
        dataStatus.getSelectionModel().select(0);

        handleTextAreaEvents();
        handleComboBoxEvents();
    }

    /**
     * Called each time text is entered to the phone number TextArea
     * Attempts to parse the value in the TextArea
     * if the TextArea contains a non-numerical value, the call button is disables
     */
    private void handleTextAreaEvents() {
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            checkPhoneNumberField();
        });

        messageArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!phoneNumberField.getText().isEmpty())
                sendSMSButton.setDisable(false);
            else sendSMSButton.setDisable(true);
        });
    }

    private void checkPhoneNumberField() {
        if(!phoneNumberField.getText().isEmpty() && endCallButton.isDisable()) {
            try {
                Integer.parseInt(phoneNumberField.getText());
                isInteger = true;
            } catch (NumberFormatException nfe) {
                isInteger = false;
            }

            if(isInteger) {
                makeCallButton.setDisable(false);
                if(!messageArea.getText().isEmpty())
                    sendSMSButton.setDisable(false);
            }
            else {
                makeCallButton.setDisable(true);
                sendSMSButton.setDisable(true);
            }
        }
        else makeCallButton.setDisable(true);
    }

    /**
     * handles all ComboBox events
     */
    private void handleComboBoxEvents() {
        batteryHealth.setOnAction(event -> {
            String health = batteryHealth.getValue().toString().toLowerCase();
            TelnetServer.batteryHealth(health);
        });

        batteryStatus.setOnAction(event -> {
            String status = batteryStatus.getValue().toString().toLowerCase().trim().replace(" ", "-");
            TelnetServer.batteryStatus(status);
        });

        charging.setOnAction(event -> {
            String status = charging.getValue().toString().toLowerCase().trim();
            switch (status) {
                case "charging":
                    TelnetServer.setCharging("on");
                    break;
                case "not charging":
                    TelnetServer.setCharging("off");
                    break;
            }
        });

        networkType.setOnAction(event -> {
            String network = networkType.getValue().toString().toLowerCase();
            Log.info(network);
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
            Log.info(signal);
            TelnetServer.gsmSignal(signal);
        });

        voiceStatus.setOnAction(event -> {
            String voice = voiceStatus.getValue().toString().toLowerCase();
            Log.info(voice);
            TelnetServer.voiceStatus(voice);
        });

        dataStatus.setOnAction(event -> {
            String data = dataStatus.getValue().toString().toLowerCase();
            Log.info(data);
            TelnetServer.dataStatus(data);
        });
    }

    /**
     * Gets the phoneNumber from the phoneNumberField
     * Simulates an incoming call to the emulator
     * @param event
     */
    @FXML
    private void handleMakeCallButtonClicked(ActionEvent event) {
        phoneNumber = phoneNumberField.getText();
        if(!phoneNumber.isEmpty()) {
            TelnetServer.makeCall(phoneNumber);
            makeCallButton.setDisable(true);
            holdCallButton.setDisable(false);
            endCallButton.setDisable(false);
        }
    }

    /**
     * Ends the call to the emulator
     * @param event
     */
    @FXML
    private void handleEndCallButtonClicked(ActionEvent event) {
        TelnetServer.endCall(phoneNumber);
        endCallButton.setDisable(true);
        holdCallButton.setText("Hold");
        isOnHold = false;
        holdCallButton.setDisable(true);
        checkPhoneNumberField();
    }

    /**
     * Holds the call to the emulator
     * @param event
     */
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

    /**
     * Sends an SMS to the emulator with the text contained in the SMSTextArea
     * @param event
     */
    @FXML
    private void handleSendSMSButtonClicked(ActionEvent event) {
        phoneNumber  = phoneNumberField.getText();
        String command =  messageArea.getText().replaceAll("\n", "\\\\n");
        TelnetServer.sendSMS(phoneNumber + " " + command);
    }

    /**
     * Initialize the buttons
     * Can do any of the following:
     * Set tooltip text
     * Set image
     * Set disabled / enabled
     * Set visible / invisible
     */
    @Override
    public void initializeButtons() {
        holdCallButton.setDisable(true);
        endCallButton.setDisable(true);
        makeCallButton.setDisable(true);
        sendSMSButton.setDisable(true);
    }
}
