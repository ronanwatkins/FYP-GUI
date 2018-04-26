package application.automation.extras;

import application.automation.CreateBatchController;
import application.device.Device;
import application.utilities.ApplicationUtils;
import application.utilities.Showable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GetTouchPositionController implements Initializable, Showable<CreateBatchController> {
    private static final Logger Log = Logger.getLogger(GetTouchPositionController.class.getName());

    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private TextField xEndField;
    @FXML
    private TextField yEndField;
    @FXML
    private TextField durationField;

    @FXML
    private Label xLabel;
    @FXML
    private Label yLabel;
    @FXML
    private Label xEndLabel;
    @FXML
    private Label yEndLabel;
    @FXML
    private Label durationLabel;

    @FXML
    private RadioButton tapRadioButton;
    @FXML
    private RadioButton swipeRadioButton;

    @FXML
    private Button OKButton;

    private static CreateBatchController controller;

    private GetTouchPositionController getTouchPositionController;

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

        System.out.println("System.out " + System.out);

        getTouchPositionController = this;
        Log.info("getTouchPositionController: " + getTouchPositionController);

        tapRadioButton.setSelected(true);
        if(tapRadioButton.isSelected()) {
            try {
                device.getCursorPosition(getTouchPositionController);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        showFields(false);
    }

    @Override
    public Initializable newWindow(CreateBatchController createBatchController, Object object) throws IOException {
        controller = createBatchController;
        Log.info("newWindow ");

        FXMLLoader fxmlLoader = new FXMLLoader(createBatchController.getClass().getResource("/application/automation/extras/GetTouchPosition.fxml"));
        Parent root = fxmlLoader.load();
        GetTouchPositionController getTouchPositionController = fxmlLoader.getController();
        root.getStylesheets().add("/application/global.css");

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Get Cursor Location");
        stage.setScene(new Scene(root));

        stage.setOnCloseRequest(event -> device.stopGettingCursorPosition());

        stage.show();

        return getTouchPositionController;
    }

    private void getCursorPositionTap() {
        device.setSwipeFlag(false);
    }

    private void getCursorPositionSwipe() {
        device.setSwipeFlag(true);
    }

    @FXML
    public void handleOKButtonClicked(ActionEvent event) {
        String text = "";

        if(yEndField.getText().equals("") && xEndField.getText().equals("")) {
            text = "shell input tap ";
            text += xField.getText() + " ";
            text += yField.getText() + " ";
            text += durationField.getText();
        } else {
            text = "shell input swipe ";
            text += xField.getText() + " ";
            text += yField.getText() + " ";
            text += xEndField.getText() + " ";
            text += yEndField.getText() + " ";
            text += durationField.getText();
        }

        controller.setCommandText(text);

        xField.setText("");
        yField.setText("");
        xEndField.setText("");
        yEndField.setText("");
        durationField.setText("");
        device.setSwipeFlag(false);
        device.stopGettingCursorPosition();

        ((Stage) OKButton.getScene().getWindow()).close();
    }

    @FXML
    public void handleTapRadioButtonClicked(ActionEvent event) {
        if(tapRadioButton.isSelected()) {
            swipeRadioButton.setSelected(false);

            showFields(false);

            durationField.setText("");
            xField.setText("");
            yField.setText("");
            xEndField.setText("");
            yEndField.setText("");

            getCursorPositionTap();
        }
    }

    @FXML
    public void handleSwipeRadioButtonClicked(ActionEvent event) {
        if(swipeRadioButton.isSelected()) {
            tapRadioButton.setSelected(false);

            showFields(true);

            xField.setText("");
            yField.setText("");

            getCursorPositionSwipe();
        }
    }

    private void showFields(boolean flag) {
        durationField.setVisible(flag);
        durationLabel.setVisible(flag);
        xEndLabel.setVisible(flag);
        yEndLabel.setVisible(flag);
        xEndField.setVisible(flag);
        yEndField.setVisible(flag);
    }


    public void setYField(double value) {
        Platform.runLater(() -> yField.setText(String.format("%.2f", value)));
    }

    public void setXField(double value) {
        Platform.runLater(() -> xField.setText(String.format("%.2f", value)));
    }

    public void setYEndField(double value) {
        Platform.runLater(() -> yEndField.setText(String.format("%.2f", value)));
    }

    public void setXEndField(double value) {
        Platform.runLater(() -> xEndField.setText(String.format("%.2f", value)));
    }

}
