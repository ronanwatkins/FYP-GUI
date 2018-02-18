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

        if(!createDirectories())
            System.out.println("Failed to make directories");
        else
            System.out.println(":)");
    }

    private boolean createDirectories() {
        boolean result = true;

        String path = System.getProperty("user.dir");

        path += "\\misc";
        File directory = new File(path);
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(path + "\\commands");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(path + "\\sensors");
        if(!directory.exists())
            result = directory.mkdir();

        return result;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
