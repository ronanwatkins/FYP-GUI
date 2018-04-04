package application.console;

import application.ADBUtil;

import application.utilities.ApplicationUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
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

    @FXML
    private Hyperlink helpLink;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        enterButton.setDisable(true);

        enterButton.setOnAction(event -> {
            String[] parameters = commandField.getText().split(" ");
            String result = ADBUtil.consoleCommand(parameters, false);

            resultArea.setText(result);
        });

        commandField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!commandField.getText().isEmpty())
                enterButton.setDisable(false);
            else enterButton.setDisable(true);
        });

        commandField.setOnAction(event -> {
            String[] parameters = commandField.getText().split(" ");
            String result;
            if(parameters[0].equals("shell"))
                result = ADBUtil.consoleCommand(parameters, true);
            else
                result = ADBUtil.consoleCommand(parameters, false);

            resultArea.setText(result);
        });

        helpLink.setOnAction(event -> {
            ApplicationUtils applicationUtils = () -> {};
            applicationUtils.browse("https://developer.android.com/studio/command-line/adb.html#issuingcommands");
        });
    }
}
