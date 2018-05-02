package application.console;

import application.utilities.ADBUtil;

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
import java.io.IOException;
import java.io.InputStreamReader;
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

    private Task<Void> runCommandTask;

    private Device device = Device.getInstance();

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
        resultArea.setWrapText(true);
        resultArea.setEditable(false);

        enterButton.setOnAction(event -> enterCommand(commandField.getText().trim()));
        commandField.setOnAction(event -> enterCommand(commandField.getText().trim()));
        commandField.textProperty().addListener((observable, oldValue, newValue) -> enterButton.setDisable(commandField.getText().isEmpty()));
        helpLink.setOnAction(event -> browse("https://developer.android.com/studio/command-line/adb.html#issuingcommands"));
    }

    /**
     * Sends the shell command ovre ADB to the connected device
     * @param command
     */
    private void enterCommand(String command) {

        resultArea.clear();
        if(runCommandTask != null)
            runCommandTask.cancel();

        final String newCommand = ADBUtil.getAdbPath() + " -s " + device.getName() + " " + command;
        Log.info("Command: " + newCommand);
        runCommandTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec(newCommand);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                doCommand(this, bufferedReader);

                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                doCommand(this, bufferedReader);

                return null;
            }
        };

        new Thread(runCommandTask).start();
    }

    private void doCommand(Task task, BufferedReader bufferedReader) throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if(task.isCancelled())
                return;

            if(line.isEmpty())
                continue;

            Log.info("line: " + line);
            final String newLine = line;
            Platform.runLater(() -> resultArea.appendText(newLine + "\n"));
        }
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
        enterButton.setDisable(true);
    }
}
