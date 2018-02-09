package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));

        Scene scene = new Scene(root);

        stage.getIcons().add(new Image("/resources/Android.png"));
        stage.setTitle("Android Sensor Emulator");
        stage.getProperties().put("hostServices", this.getHostServices());
        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setScene(scene);
        stage.show();

        TelnetServer.connect();

        ADBUtil.findADB();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
