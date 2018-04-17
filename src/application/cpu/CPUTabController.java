package application.cpu;

import application.ADBUtil;
import application.device.Device;
import application.logcat.LogCatTabController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class CPUTabController extends LogCatTabController implements Initializable {
    private static final Logger Log = Logger.getLogger(CPUTabController.class.getName());

    @FXML
    private LineChart<Number, Number> chart;

    private XYChart.Series<Number, Number> dataSeries;

    @FXML
    private NumberAxis xAxis;

    private Timeline animation;

    private double sequence = 0;

    private double y = 10;

    private final int MAX_DATA_POINTS = 25, MAX = 100, MIN = 5;

    private Device device = Device.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // create timeline to add new data every 60th of second
     ///   c

        createContent();

        animation = new Timeline();
        animation.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent actionEvent) -> plotTime()));

        animation.setCycleCount(Animation.INDEFINITE);
    }

    @FXML
    @Override
    protected void handleStartButtonClicked(MouseEvent event) {
        if(startButton.getText().equals("Start"))
            play();
        else
            stop();
    }

    public Parent createContent() {

        final NumberAxis yAxis = new NumberAxis(MIN - 1, MAX + 1, 1);
        //chart = new LineChart<>(xAxis, yAxis);


        // setup chart
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Animated Line Chart");
        xAxis.setLabel("X Axis");
        xAxis.setForceZeroInRange(false);

        yAxis.setLabel("Y Axis");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));

        // add starting data
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");

        // create some starting data
        dataSeries.getData().add(new XYChart.Data<>(++sequence, y));

        chart.getData().add(dataSeries);

        return chart;
    }



    private void plotTime() {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Process process = Runtime.getRuntime().exec(ADBUtil.getAdbPath() + " -s " + device.getName() + " shell \"top -d 1 | grep com.snapchat.android\"");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if(stopFlag)
                        break;


                    line = line.replace("   ", " ").replace("  ", " ").replace("%", "");
                    System.out.println(line);

                    String CPUString = line.split(" ")[2];
                    System.out.println(CPUString);

                    int CPUPercentage = Integer.parseInt(CPUString)/10;

                    Platform.runLater(() ->dataSeries.getData().add(new XYChart.Data<>(++sequence, CPUPercentage)));

                    System.out.println("sequence: " + sequence);
                }
                return null;
            }
        };

        new Thread(task).start();


    }

    public void play() {
        Log.info("Playing...");
        animation.play();
        startButton.setText("Stop");
    }

    public void stop() {
        Log.info("Stopping...");
        animation.pause();
        startButton.setText("Start");
    }
}
