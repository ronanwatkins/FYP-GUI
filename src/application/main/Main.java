package application.main;

import application.sensors.SensorsTabController;
import application.utilities.ADBUtil;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;

public class Main extends Application {
    private static final Logger Log = Logger.getLogger(Main.class);

    public static final String APPLICATION_DIRECTORY = System.getProperty("user.dir") + "\\misc";

    private static HostServices services;

    @Override
    public void start(Stage stage) throws Exception {
        PropertyConfigurator.configure(Main.class.getClassLoader().getResource("log4j.properties"));
        Log.info("Starting application");
        services = getHostServices();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLMain.fxml"));
        final Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/application/main/global.css");

        stage.getIcons().add(new Image("/resources/Android.png"));
        stage.setTitle("Android Sensor Emulator");
        stage.getProperties().put("hostServices", this.getHostServices());
        stage.setOnCloseRequest(e -> exit());
        stage.setScene(scene);
        stage.setMaximized(true);

        Screen screen = Screen.getPrimary();
        if(Screen.getScreens().get(1) != null)
            screen = Screen.getScreens().get(1);

        Rectangle2D primaryScreenBounds = screen.getVisualBounds();
        stage.setMaxWidth(primaryScreenBounds.getWidth());
        stage.setMaxHeight(primaryScreenBounds.getHeight());

        stage.setOnShowing(event -> Platform.runLater(() -> SensorsTabController.getSensorsTabController().initializePhone()));
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            SensorsTabController.getSensorsTabController().alterPhoneLayout();
        });
        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            SensorsTabController.getSensorsTabController().alterPhoneLayout();
        });

        stage.show();

        ADBUtil.initADB();

        if (!createDirectories())
            Log.error("Could not create all application directories");
    }

    public static HostServices hostServices() {
        return services;
    }

    private boolean createDirectories() {
        boolean result = true;

        File directory = new File(APPLICATION_DIRECTORY);
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\automation");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\sensors");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\files");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\location");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\applications");
        if(!directory.exists())
            result = directory.mkdir();

        directory = new File(APPLICATION_DIRECTORY + "\\logcat\\filters");
        if(!directory.exists())
            result = directory.mkdir();

        return result;
    }

    public static void exit() {
        Log.info("Closing application");
        ADBUtil.disconnect();
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
