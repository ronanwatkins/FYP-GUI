package application;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));
       /* try {
            FXMLLoader loader1 = new FXMLLoader(getClass().getResource("SensorsTab.fxml"));
            loader1.setRoot(this);
            loader1.setController(this);
            loader1.setClassLoader(getClass().getClassLoader());
            //loader1.load();
        } catch (Exception e ){
            throw new RuntimeException(e);
        }*/
        Scene scene = new Scene(root);
        stage.setTitle("Android Sensor Emulator");
       // stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();

        try {
            TelnetServer.connect();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
