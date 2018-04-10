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

public class ConsoleTabController implements Initializable, ApplicationUtils {

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
        initializeButtons();
        resultArea.setWrapText(true);
        resultArea.setEditable(false);

        enterButton.setOnAction(event -> resultArea.setText(ADBUtil.consoleCommand(commandField.getText())));
        commandField.setOnAction(event -> resultArea.setText(ADBUtil.consoleCommand(commandField.getText())));
        commandField.textProperty().addListener((observable, oldValue, newValue) -> enterButton.setDisable(commandField.getText().isEmpty()));
        helpLink.setOnAction(event -> browse("https://developer.android.com/studio/command-line/adb.html#issuingcommands"));
    }

    @Override
    public void initializeButtons() {
        enterButton.setDisable(true);
    }
}
