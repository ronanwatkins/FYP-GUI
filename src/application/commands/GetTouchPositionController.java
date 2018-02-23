package application.commands;

import application.ADBUtil;
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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GetTouchPositionController implements Initializable {

    @FXML
    private TextField xField;
    @FXML
    private TextField yField;
    @FXML
    private TextField xEndField;
    @FXML
    private TextField yEndField;

    @FXML
    private Label xLabel;
    @FXML
    private Label yLabel;
    @FXML
    private Label xEndLabel;
    @FXML
    private Label yEndLabel;

    @FXML
    private RadioButton tapRadioButton;
    @FXML
    private RadioButton swipeRadioButton;

    @FXML
    private Button OKButton;

    private static CommandsTabController controller;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tapRadioButton.setSelected(true);
        if(tapRadioButton.isSelected()) {
            try {
                ADBUtil.getCursorPosition(this);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        xEndLabel.setVisible(false);
        yEndLabel.setVisible(false);
        xEndField.setVisible(false);
        yEndField.setVisible(false);
    }

    private void getCursorPositionTap() {
        ADBUtil.setSwipeFlag(false);
    }

    private void getCursorPositionSwipe() {
        ADBUtil.setSwipeFlag(true);
    }

    @FXML
    public void handleOKButtonClicked(ActionEvent event) {
        String text = "";

        if(yEndField.getText().equals("") && xEndField.getText().equals("")) {
            text = "shell input tap ";
            text += xField.getText() + " ";
            text += yField.getText();
        } else {
            text = "shell input swipe ";
            text += xField.getText() + " ";
            text += yField.getText() + " ";
            text += xEndField.getText() + " ";
            text += yEndField.getText();
        }

        System.out.println("Text: " + text);
        controller.setCommandText(text);
        ((Stage) OKButton.getScene().getWindow()).close();
    }

    @FXML
    public void handleTapRadioButtonClicked(ActionEvent event) {
        if(tapRadioButton.isSelected()) {
            swipeRadioButton.setSelected(false);

            xEndLabel.setVisible(false);
            yEndLabel.setVisible(false);
            xEndField.setVisible(false);
            yEndField.setVisible(false);

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

            xEndLabel.setVisible(true);
            yEndLabel.setVisible(true);
            xEndField.setVisible(true);
            yEndField.setVisible(true);

            xField.setText("");
            yField.setText("");

            getCursorPositionSwipe();
        }
    }

    public static void showScreen(CommandsTabController commandsTabController) throws IOException {
        controller = commandsTabController;

        FXMLLoader fxmlLoader = new FXMLLoader(commandsTabController.getClass().getResource("/application/commands/GetTouchPosition.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Get Cursor Location");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public void setYField(double value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                yField.setText(String.format("%.2f", value));
            }
        });
    }

    public void setXField(double value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                xField.setText(String.format("%.2f", value));
            }
        });
    }

    public void setYEndField(double value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                yEndField.setText(String.format("%.2f", value));
            }
        });
    }

    public void setXEndField(double value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                xEndField.setText(String.format("%.2f", value));
            }
        });
    }
}
