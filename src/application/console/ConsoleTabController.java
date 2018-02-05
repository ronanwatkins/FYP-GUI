package application.console;

import application.ADBUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ConsoleTabController implements Initializable {

    @FXML
    private TextField commandField;

    @FXML
    private TextArea resultArea;

    @FXML
    private Button enterButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        resultArea.setWrapText(true);
        enterButton.setDisable(true);

        enterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String[] parameters = commandField.getText().split(" ");
                resultArea.setText(ADBUtil.consoleCommand(parameters));
            }
        });

        commandField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!commandField.getText().isEmpty())
                enterButton.setDisable(false);
            else enterButton.setDisable(true);
        });

        commandField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String[] parameters = commandField.getText().split(" ");
                resultArea.setText(ADBUtil.consoleCommand(parameters));
            }
        });
    }
}
