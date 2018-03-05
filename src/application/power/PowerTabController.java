package application.power;

import application.TelnetServer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import javax.annotation.Generated;
import java.net.URL;
import java.util.ResourceBundle;


public class PowerTabController implements Initializable{

    @FXML
    private Slider batterySlider;

    @FXML
    private Label batteryLabel;

    @FXML
    private ComboBox batteryHealth;
    @FXML
    private ComboBox batteryStatus;
    @FXML
    private ComboBox charging;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        batterySlider.valueProperty().addListener(
                (observable, oldvalue, newvalue) ->
                {
                    int batteryValue = newvalue.intValue();
                    batteryLabel.setText(batteryValue+"");
                    TelnetServer.powerCapacity(batteryValue+"");
                } );

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
    }
}
