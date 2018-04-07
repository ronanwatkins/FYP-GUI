package application.automation.extras;

import application.ADBUtil;
import application.automation.CreateBatchController;
import application.utilities.Showable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RecordInputsController implements Initializable, Showable<CreateBatchController> {

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    private static CreateBatchController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void newWindow(CreateBatchController createBatchController, File file) throws IOException {
        controller = createBatchController;

        FXMLLoader fxmlLoader = new FXMLLoader(createBatchController.getClass().getResource("/application/automation/extras/RecordInputs.fxml"));
        Parent root = fxmlLoader.load();
        root.getStylesheets().add("/application/global.css");

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Record Inputs");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void handleStartButtonClicked(ActionEvent event) {
        try {
            ADBUtil.recordInputValues(this);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    @FXML
    private void handleStopButtonClicked(ActionEvent event) {
        System.out.println("stop button pressed");
        ADBUtil.setStopRecordingFlag(true);
    }
}
