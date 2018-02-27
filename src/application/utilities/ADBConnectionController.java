package application.utilities;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ADBConnectionController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void showScreen() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/application/utilities/ADBConnection.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Get Cursor Location");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
