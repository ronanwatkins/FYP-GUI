package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    private static Parent root;
    @Override
    public void start(Stage stage) throws Exception {

        try {
            root = FXMLLoader.load(getClass().getResource("FXMLMain.fxml"));
            Scene scene = new Scene(root);

            stage.getIcons().add(new Image("/resources/Android.png"));
            stage.setTitle("Android Sensor Emulator");
            stage.getProperties().put("hostServices", this.getHostServices());
            stage.setOnCloseRequest(e -> {
                ADBUtil.disconnect();
                Platform.exit();
                System.exit(0);
            });
            stage.setScene(scene);
            stage.show();

            TelnetServer.connect();
            ADBUtil.initADB();

            if (!createDirectories())
                System.out.println("Failed to make directories");
        }
        finally {
            //System.out.println("Disconnecting");
            //ADBUtil.disconnect();
        }


    }

    public static ObservableList<Node> getChildren() {
        return root.getChildrenUnmodifiable();
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

        directory = new File(path + "\\files");
        if(!directory.exists())
            result = directory.mkdir();

        return result;
    }

    public static void main(String[] args) {
        launch(args);

//        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//            public void run() {
//                System.out.println("Disconnecting");
//                ADBUtil.disconnect();
//            }
//        }));
    }
}
