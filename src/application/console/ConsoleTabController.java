package application.console;

import application.ADBUtil;

import application.utilities.ApplicationUtils;
import application.device.Device;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class ConsoleTabController implements Initializable, ApplicationUtils {

    @FXML
    private TextField commandField;

    @FXML
    private TextArea resultArea;

    @FXML
    private Button enterButton;

    @FXML
    private Hyperlink helpLink;

    private Device device = Device.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeButtons();
        resultArea.setWrapText(true);
        resultArea.setEditable(false);

        enterButton.setOnAction(event -> enterCommand(commandField.getText().trim()));
        commandField.setOnAction(event -> enterCommand(commandField.getText().trim()));
        commandField.textProperty().addListener((observable, oldValue, newValue) -> enterButton.setDisable(commandField.getText().isEmpty()));
        helpLink.setOnAction(event -> browse("https://developer.android.com/studio/command-line/adb.html#issuingcommands"));
    }

    private void enterCommand(String command) {
        final String newCommand = ADBUtil.getAdbPath() + " -s " + device.getName() + " " + command;
        Log.info("Command: " + newCommand);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec(newCommand);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                System.out.println("gonna read " + bufferedReader.ready());

               // bufferedReader.read();

                Stream<String> stringStream = bufferedReader.lines();
                for(String string : (String[])stringStream.toArray())
                    System.out.println("string: " + string);


                System.out.println("read dude");

                if(command.equals("shell")) {
                    System.out.println("here kiddd");
                    System.out.println(bufferedReader.readLine());
                } else {
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        stringBuilder.append(line).append("\n");
                        Platform.runLater(() -> resultArea.setText(stringBuilder.toString()));
                    }
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    @Override
    public void initializeButtons() {
        enterButton.setDisable(true);
    }
}
