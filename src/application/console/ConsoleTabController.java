package application.console;

import application.ADBUtil;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

import java.awt.Desktop;

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

        //HostServices hostServices = (HostServices)this..getProperties().get("hostServices");



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
            //hostServices.showDocument("https://developer.android.com/studio/command-line/adb.html#issuingcommands");

            try {
                URI uri = new URI("https://developer.android.com/studio/command-line/adb.html#issuingcommands");
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(uri);
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });
    }
}
