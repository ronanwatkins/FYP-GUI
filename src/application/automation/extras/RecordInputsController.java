package application.automation.extras;

import application.ADBUtil;
import application.automation.CreateBatchTabController;
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

public class RecordInputsController implements Initializable, Showable<CreateBatchTabController> {

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    private static CreateBatchTabController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void newWindow(CreateBatchTabController createBatchTabController, File file) throws IOException {
        controller = createBatchTabController;

        FXMLLoader fxmlLoader = new FXMLLoader(createBatchTabController.getClass().getResource("/application/automation/extras/RecordInputs.fxml"));
        Parent root = fxmlLoader.load();

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