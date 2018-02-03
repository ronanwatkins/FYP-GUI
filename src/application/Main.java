package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));

        Scene scene = new Scene(root);

        stage.getIcons().add(new Image("/resources/Android.png"));
        stage.setTitle("Android Sensor Emulator");
        // stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();


        TelnetServer.connect();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
