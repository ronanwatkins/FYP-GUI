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

//    public Stage getStage() {
//        if(this.stage==null)
//            this.stage = (Stage) this.mainAnchor1.getScene().getWindow();
//        return stage;
//    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        resultArea.setWrapText(true);
        resultArea.setEditable(false);
        enterButton.setDisable(true);

        //HostServices hostServices = (HostServices)this..getProperties().get("hostServices");



        enterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String[] parameters = commandField.getText().split(" ");
                String result = ADBUtil.consoleCommand(parameters, false);
                Text text = new Text(result);
                text.setFill(Color.RED);
                resultArea.setText(text.getText());
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
                resultArea.setText(ADBUtil.consoleCommand(parameters,true));
            }
        });

        helpLink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
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
            }
        });
    }
}
