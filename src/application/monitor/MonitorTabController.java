package application.monitor;

import application.applications.ApplicationTabController;
import application.device.AndroidApplication;
import application.device.Device;
import application.monitor.model.CPUMonitor;
import application.utilities.ADB;
import application.utilities.ApplicationUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.Collections;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class MonitorTabController extends ApplicationTabController implements Initializable, ApplicationUtils {
    private static final Logger Log = Logger.getLogger(MonitorTabController.class.getName());

    @FXML
    private Label cpuLabel;

    @FXML
    private Button startButton;
    @FXML
    private Button refreshButton;

    @FXML
    private SplitPane verticalPane;

    @FXML
    private AreaChart<Number, Number> CPUChart;
    @FXML
    private AreaChart<Number, Number> MemoryChart;
    @FXML
    private AreaChart<Number, Number> NetworkChart;

    private XYChart.Series<Number, Number> CPUDataSeries;
    private Timeline CPUAnimation;

    private XYChart.Series<Number, Number> MemoryDataSeries;
    private Timeline MemoryAnimation;

    private XYChart.Series<Number, Number> NetworkDataSeries;
    private Timeline NetworkAnimation;

    private int sequence = 0;

    private Device device = Device.getInstance();

    private Thread monitorServiceThread;
    private MonitorService monitorService = MonitorService.getInstance();
    private CPUMonitor cpuMonitor = CPUMonitor.getInstance();

    private Random rand = new Random();

    private AtomicInteger CPUPercentage = new AtomicInteger(0);


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        verticalPane.setDividerPositions(0.3333f, 0.6666f, 0.9999f);

        initializeButtons();
        initializeCPUChart();
        initializeMemoryChart();
        initializeNetworkChart();

    }

    private void initializeNetworkChart() {
        NetworkAnimation = new Timeline();
        NetworkAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> {
            NetworkDataSeries.getData().add(new XYChart.Data<>(sequence, rand.nextInt(100)));
            if(NetworkDataSeries.getData().size() > 10) {
                NetworkDataSeries.getData().remove(0);
            }
        }));
        NetworkAnimation.setCycleCount(Animation.INDEFINITE);

        NetworkDataSeries = new XYChart.Series<>();

        NetworkChart.setLegendVisible(false);

        NetworkChart.getData().add(NetworkDataSeries);
        NetworkChart.setCreateSymbols(false);

        NetworkChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: #85e085;");
        NetworkChart.setStyle("CHART_COLOR_1: #49d049;");
    }

    private void initializeMemoryChart() {
        MemoryAnimation = new Timeline();
        MemoryAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> {
            MemoryDataSeries.getData().add(new XYChart.Data<>(sequence, rand.nextInt(100)));
            if(MemoryDataSeries.getData().size() > 10) {
                MemoryDataSeries.getData().remove(0);
            }
        }));
        MemoryAnimation.setCycleCount(Animation.INDEFINITE);

        MemoryDataSeries = new XYChart.Series<>();
        MemoryDataSeries.setName("");

        MemoryChart.setLegendVisible(false);

        MemoryChart.getData().add(MemoryDataSeries);
        MemoryChart.setCreateSymbols(false);

        MemoryChart.lookup(".default-color0.chart-series-area-fill").setStyle("-fx-fill: #99d6ff;");
        MemoryChart.setStyle("CHART_COLOR_1: #33adff;");
    }

    private void initializeCPUChart() {
        CPUAnimation = new Timeline();
        CPUAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(500), event -> {
            CPUDataSeries.getData().add(new XYChart.Data<>(++sequence, rand.nextInt(100)));
            if(CPUDataSeries.getData().size() > 10) {
                CPUDataSeries.getData().remove(0);
            }
        }));
        CPUAnimation.setCycleCount(Animation.INDEFINITE);

        CPUDataSeries = new XYChart.Series<>();
        CPUDataSeries.setName("");

        CPUChart.setLegendVisible(false);

        CPUChart.getData().add(CPUDataSeries);
        CPUChart.setCreateSymbols(false);

        CPUDataSeries.getNode().setStyle("-fx-bar-fill: #4CAF50");
    }

    @FXML
    protected void handleStartButtonClicked(MouseEvent event) {
        if(startButton.getText().equals("Start"))
            play();
        else
            stop();
    }

    public void play() {
        Log.info("Playing...");

//        Task<Void> task = new Task<Void>() {
//            @Override
//            protected Void call() throws IOException {
//                updateCPUPercentage();
//                return null;
//            }
//        };
//        task.setOnFailed(event -> Log.error(task.getException().getMessage(), task.getException()));

//        new Thread(task).start();


        monitorServiceThread = new Thread(monitorService);
        monitorServiceThread.start();

        bindLabels();

        MemoryAnimation.play();
        CPUAnimation.play();
        NetworkAnimation.play();

        startButton.setText("Stop");
    }

    private void bindLabels() {
        cpuLabel.textProperty().bindBidirectional(cpuMonitor.CPUVendorProperty());
    }

    public void stop() {
        Log.info("Stopping...");

        Log.info("GOING TO INTERRUPT!!!!!!");
        //monitorService.interrupt();
        Log.info("is cancelled? " + monitorService.cancel());
        monitorServiceThread.interrupt();
        Log.info("INTERRUPTED!!!!!!!");

        //monitorServiceThread.interrupt();

        MemoryAnimation.pause();
        CPUAnimation.pause();
        NetworkAnimation.pause();

        startButton.setText("Start");
    }

    @FXML
    private void handleAppsListViewClicked(MouseEvent mouseEvent) {
        String applicationName = appsOnDeviceListView.getSelectionModel().getSelectedItem();

        Task<AndroidApplication> task = new Task<AndroidApplication>() {
            @Override
            protected AndroidApplication call() {
                return new AndroidApplication(applicationName);
            }
        };
        task.setOnSucceeded(event -> {
            device.setSelectedApplication(task.getValue());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleRefreshButtonClicked(ActionEvent event) {
        Log.info("");

        updateDeviceListView();
    }

    @Override
    public void initializeButtons() {
        setImage("/resources/refresh.png", null, refreshButton);
    }
}
