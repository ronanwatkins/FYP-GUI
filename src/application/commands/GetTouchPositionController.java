package application.commands;

import application.ADBUtil;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GetTouchPositionController implements Initializable {

    @FXML
    private TextField xField;
    @FXML
    private TextField yField;

    @FXML
    private Button OKButton;

    public GetTouchPositionController() {}
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //xField.setDisable(false);
        //yField.setDisable(false);

        try {
            ADBUtil.getCursorPosition(getClass());
        } catch (Exception ee) {
            ee.printStackTrace();
        }

        OKButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent event) {
                String text = "shell input tap ";
                text += xField + " ";
                text += yField;

                CommandsTabController.setCommandText(text);
            }
        });
    }

    public static void showScreen(Class callingClass) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(callingClass.getResource("/application/commands/GetTouchPosition.fxml"));

        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Get Cursor Location");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void handleOKButtonClicked(ActionEvent event) {
        String text = "shell input tap ";
        text += xField + " ";
        text += yField;

        CommandsTabController.setCommandText(text);
    }

    public void setYField(Integer value) {
      //  System.out.println("xValue to String: " + value.toString());
       // if(value != null)
            yField.setText(":):):):)");
    }

    public void setXField(Integer value) {
       // if(value != null)
            xField.setText(":)(:):):):)");
    }
}
